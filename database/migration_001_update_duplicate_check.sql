-- Migration: Update trigger to allow new promotion requests after calibration completed
-- Изменяет триггер prevent_duplicate_promotion_request чтобы разрешать создание новых заявок
-- после того как предыдущая заявка завершена (calibration_completed, approved, rejected)

CREATE OR REPLACE FUNCTION prevent_duplicate_promotion_request()
RETURNS TRIGGER AS $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM grading2.promotion_requests
        WHERE employee_id = NEW.employee_id
          AND requested_grade_id = NEW.requested_grade_id
          AND status IN ('pending', 'under_review', 'ready_for_calibration', 'in_calibration', 'calibration_completed')
          AND id != COALESCE(NEW.id, 0)
    ) THEN
        RAISE EXCEPTION 'Active promotion request already exists for this employee and grade';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Триггер уже существует, поэтому не нужно создавать его заново
-- CREATE TRIGGER trg_check_duplicate_promotion
-- BEFORE INSERT OR UPDATE ON promotion_requests
-- FOR EACH ROW EXECUTE FUNCTION prevent_duplicate_promotion_request();
