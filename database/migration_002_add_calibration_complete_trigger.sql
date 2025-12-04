-- Migration: Add trigger to update promotion requests status when calibration is completed
-- Date: 2025-12-04
-- Description: Automatically updates promotion_requests status from 'in_calibration' to 'calibration_completed'
--              when calibration status changes to 'completed'

-- Create function for trigger
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

-- Create trigger
DROP TRIGGER IF EXISTS trg_update_pr_on_calibration_complete ON grading2.calibrations;
CREATE TRIGGER trg_update_pr_on_calibration_complete
AFTER UPDATE ON grading2.calibrations
FOR EACH ROW EXECUTE FUNCTION grading2.update_promotion_requests_on_calibration_complete();

-- Update existing promotion requests for already completed calibrations
UPDATE grading2.promotion_requests pr
SET status = 'calibration_completed'
WHERE status = 'in_calibration'
  AND EXISTS (
    SELECT 1 FROM grading2.calibrations c
    WHERE c.id = pr.calibration_id
      AND c.status = 'completed'
  );
