-- Add experience_requirement column to store exact text from job posting
ALTER TABLE job_postings 
ADD COLUMN experience_requirement VARCHAR(100) AFTER experience_level;

-- Rename experience_level to experience_category for clarity
ALTER TABLE job_postings 
CHANGE COLUMN experience_level experience_category ENUM('ENTRY', 'JUNIOR', 'MID', 'SENIOR', 'LEAD', 'ANY') DEFAULT 'ANY';

-- Update existing data based on current experience_level values
UPDATE job_postings 
SET experience_requirement = CASE
    WHEN experience_category = 'JUNIOR' THEN '신입'
    WHEN experience_category = 'MID' THEN '3~5년'
    WHEN experience_category = 'SENIOR' THEN '5년 이상'
    WHEN experience_category = 'LEAD' THEN '8년 이상'
    WHEN experience_category = 'ANY' THEN '경력무관'
    ELSE '경력무관'
END
WHERE experience_requirement IS NULL;