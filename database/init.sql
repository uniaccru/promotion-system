-- Create schema
CREATE SCHEMA IF NOT EXISTS grading2;

-- Set search path
SET search_path TO grading2;

-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Employees table
CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id),
    full_name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    role TEXT NOT NULL,
    hire_date DATE NOT NULL,
    department TEXT NOT NULL,
    review_period TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Grades table
CREATE TABLE grades (
    id BIGSERIAL PRIMARY KEY,
    name TEXT UNIQUE NOT NULL,
    description TEXT NOT NULL
);

CREATE TABLE goal_assignments (
    id BIGSERIAL PRIMARY KEY,
    goal_id BIGINT NOT NULL REFERENCES goals(id),
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    due_date DATE NOT NULL,
    status TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Manager evaluations table
CREATE TABLE manager_evaluations (
    id BIGSERIAL PRIMARY KEY,
    evaluator_id BIGINT NOT NULL REFERENCES employees(id),
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    review_period TEXT NOT NULL,
    score NUMERIC NOT NULL,
    comment TEXT NOT NULL,
    nominated_for_promotion BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Calibrations table
CREATE TABLE calibrations (
    id BIGSERIAL PRIMARY KEY,
    grade_id BIGINT NOT NULL REFERENCES grades(id),
    created_by BIGINT NOT NULL REFERENCES employees(id),
    status TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Promotion requests table
CREATE TABLE promotion_requests (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    requested_grade_id BIGINT NOT NULL REFERENCES grades(id),
    submitted_by_id BIGINT NOT NULL REFERENCES employees(id),
    status_changed_by_id BIGINT NOT NULL REFERENCES employees(id),
    calibration_id BIGINT REFERENCES calibrations(id),
    justification TEXT NOT NULL,
    evidence TEXT NOT NULL,
    review_period TEXT NOT NULL,
    status TEXT NOT NULL,
    hr_comment TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Promotion request reviewers table
CREATE TABLE promotion_request_reviewers (
    promotion_request_id BIGINT NOT NULL REFERENCES promotion_requests(id),
    reviewer_id BIGINT NOT NULL REFERENCES employees(id),
    decision TEXT NOT NULL,
    comment TEXT NOT NULL,
    decided_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (promotion_request_id, reviewer_id)
);

-- Grade history table
CREATE TABLE grade_history (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    old_grade_id BIGINT NOT NULL REFERENCES grades(id),
    new_grade_id BIGINT NOT NULL REFERENCES grades(id),
    changed_by BIGINT NOT NULL REFERENCES employees(id),
    reason TEXT NOT NULL,
    changed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Comparisons table
CREATE TABLE comparisons (
    id BIGSERIAL PRIMARY KEY,
    calibration_id BIGINT NOT NULL REFERENCES calibrations(id),
    candidate_a_id BIGINT NOT NULL REFERENCES employees(id),
    candidate_b_id BIGINT NOT NULL REFERENCES employees(id),
    decided_by BIGINT NOT NULL REFERENCES employees(id),
    winner_id BIGINT NOT NULL REFERENCES employees(id),
    decided_at TIMESTAMPTZ NOT NULL
);

-- Calibration evaluators table (many-to-many relationship)
CREATE TABLE grading2.calibration_evaluators (
    calibration_id BIGINT NOT NULL REFERENCES grading2.calibrations(id) ON DELETE CASCADE,
    evaluator_id BIGINT NOT NULL REFERENCES grading2.employees(id) ON DELETE CASCADE,
    PRIMARY KEY (calibration_id, evaluator_id)
);

-- Indexes
CREATE INDEX idx_pr_emp_grade_status ON promotion_requests(employee_id, requested_grade_id, status) 
WHERE status IN ('pending', 'under_review', 'ready_for_calibration', 'in_calibration');

CREATE INDEX idx_grade_history_emp_latest ON grade_history(employee_id, changed_at DESC);
CREATE INDEX idx_pr_calibration_status ON promotion_requests(calibration_id, status);
CREATE INDEX idx_comparisons_calibration_candidates ON comparisons(calibration_id, candidate_a_id, candidate_b_id);
CREATE INDEX idx_manager_eval_emp_created ON manager_evaluations(employee_id, created_at DESC);
CREATE INDEX idx_comparisons_calibration ON comparisons(calibration_id);
CREATE INDEX idx_comparisons_candidate_a ON comparisons(candidate_a_id);
CREATE INDEX idx_comparisons_candidate_b ON comparisons(candidate_b_id);
CREATE INDEX idx_calibration_evaluators_calibration ON grading2.calibration_evaluators(calibration_id);
CREATE INDEX idx_calibration_evaluators_evaluator ON grading2.calibration_evaluators(evaluator_id);

-- Triggers
CREATE OR REPLACE FUNCTION create_grade_history_on_approval()
RETURNS TRIGGER AS $$
DECLARE
    current_grade_id BIGINT;
BEGIN
    IF NEW.status = 'approved' AND OLD.status != 'approved' THEN
        SELECT new_grade_id INTO current_grade_id
        FROM grading2.grade_history
        WHERE employee_id = NEW.employee_id
        ORDER BY changed_at DESC
        LIMIT 1;
        
        INSERT INTO grading2.grade_history (employee_id, old_grade_id, new_grade_id, changed_by, reason)
        VALUES (
            NEW.employee_id,
            current_grade_id,
            NEW.requested_grade_id,
            NEW.status_changed_by_id,
            'Promotion request approved: ' || NEW.id
        );
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_promotion_approved
AFTER UPDATE ON promotion_requests
FOR EACH ROW EXECUTE FUNCTION create_grade_history_on_approval();

CREATE OR REPLACE FUNCTION prevent_duplicate_promotion_request()
RETURNS TRIGGER AS $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM grading2.promotion_requests
        WHERE employee_id = NEW.employee_id
          AND requested_grade_id = NEW.requested_grade_id
          AND status IN ('pending', 'under_review', 'ready_for_calibration', 'in_calibration')
          AND id != COALESCE(NEW.id, 0)
    ) THEN
        RAISE EXCEPTION 'Active promotion request already exists for this employee and grade';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_check_duplicate_promotion
BEFORE INSERT OR UPDATE ON promotion_requests
FOR EACH ROW EXECUTE FUNCTION prevent_duplicate_promotion_request();

CREATE OR REPLACE FUNCTION check_calibration_candidates()
RETURNS TRIGGER AS $$
DECLARE
    candidate_count INT;
BEGIN
    IF NEW.status = 'active' AND OLD.status != 'active' THEN
        SELECT COUNT(DISTINCT pr.employee_id)
        INTO candidate_count
        FROM grading2.promotion_requests pr
        WHERE pr.calibration_id = NEW.id;

        IF candidate_count < 3 THEN
            RAISE EXCEPTION 'Calibration package must have at least 3 candidates, found %', candidate_count;
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_validate_calibration_activation
BEFORE UPDATE ON calibrations
FOR EACH ROW EXECUTE FUNCTION check_calibration_candidates();

-- Update promotion requests status when calibration is completed
CREATE OR REPLACE FUNCTION grading2.update_promotion_requests_on_calibration_complete()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status = 'completed' AND OLD.status != 'completed' THEN
        UPDATE grading2.promotion_requests
        SET status = 'calibration_completed'
        WHERE calibration_id = NEW.id
          AND status = 'in_calibration';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_pr_on_calibration_complete
AFTER UPDATE ON grading2.calibrations
FOR EACH ROW EXECUTE FUNCTION grading2.update_promotion_requests_on_calibration_complete();

-- Insert initial grades
INSERT INTO grades (name, description) VALUES
    ('Junior', 'Entry level position'),
    ('Middle', 'Intermediate level position'),
    ('Senior', 'Advanced level position'),
    ('Lead', 'Leadership position');

-- Seed default HR user for initial access
INSERT INTO users (username, password_hash)
VALUES ('hr1', '$2y$10$XtgQquZHe8nNiLUZMS/dHug/iuovE3y4FmP31cEJzyYeBm7hXdPcy');

INSERT INTO employees (user_id, full_name, email, role, hire_date, department, review_period)
VALUES (
    (SELECT id FROM users WHERE username = 'hr1'),
    'hr1',
    'hr1@example.com',
    'hr',
    '2024-01-01',
    'Human Resources',
    'annual'
);