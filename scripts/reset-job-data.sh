#!/bin/bash

# Reset job data to force re-initialization with TechStack relationships

echo "Clearing job data to force re-initialization..."

# MySQL commands to clear job-related tables
mysql_commands="
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE job_tech_stacks;
TRUNCATE TABLE user_saved_jobs;
TRUNCATE TABLE job_postings;
TRUNCATE TABLE tech_stacks;
TRUNCATE TABLE companies;
SET FOREIGN_KEY_CHECKS = 1;
"

# Execute MySQL commands
docker exec asyncsite-mysql sh -c "mysql -u root -p'asyncsite_root_2024!' job_db -e \"$mysql_commands\""

echo "Job data cleared. Restarting job-navigator service..."

# Restart the service
docker-compose -f /Users/Rene/Documents/rene/project/asyncsite/job-navigator-service/docker-compose.job-navigator-only.yml restart

echo "Service restarted. Data will be re-initialized with TechStack relationships."