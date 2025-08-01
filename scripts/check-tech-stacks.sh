#!/bin/bash

echo "Checking job_tech_stacks table..."

mysql_command="
SELECT 
    jp.id,
    jp.title,
    COUNT(jts.tech_stack_id) as tech_stack_count,
    GROUP_CONCAT(ts.name) as tech_stacks
FROM job_postings jp
LEFT JOIN job_tech_stacks jts ON jp.id = jts.job_posting_id
LEFT JOIN tech_stacks ts ON jts.tech_stack_id = ts.id
GROUP BY jp.id, jp.title
LIMIT 5;
"

docker exec asyncsite-mysql sh -c "mysql -u root -p'asyncsite_root_2024!' job_db -e \"$mysql_command\""