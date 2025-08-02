# word boundary 수정을 위한 테스트
import re

# 한글과 영어가 섞여있을 때도 작동하는 패턴
def create_tech_pattern(tech_variations):
    """Create pattern that works with Korean text"""
    # Simply use variations without word boundary for now
    return '(' + '|'.join(tech_variations) + ')'

# 테스트
test_texts = [
    "Java, Kotlin을 사용하고 있고",
    "MySQL, Redis, Elasticsearch를 사용하며",
    "모니터링은 Datadog, Grafana를 사용하고 있으며",
    "협업 도구로는 Jira, Confluence, Slack을 사용합니다",
    "인프라는 AWS 기반으로 Docker, k8s를 활용합니다"
]

patterns = {
    'Kotlin': r'(Kotlin|kotlin|KOTLIN)',
    'Elasticsearch': r'(Elasticsearch|ElasticSearch|elastic search)',
    'Grafana': r'(Grafana|grafana)',
    'Slack': r'(Slack|slack|SLACK)',
    'Kubernetes': r'(Kubernetes|K8s|k8s|K8S)',
}

print("=== 패턴 매칭 테스트 ===")
for text in test_texts:
    print(f"\n텍스트: {text}")
    for tech, pattern in patterns.items():
        match = re.search(pattern, text, re.IGNORECASE)
        if match:
            print(f"  {tech}: 매칭됨 - '{match.group()}'")
            
# 현재 TECH_PATTERNS의 정규식 문제 확인
print("\n=== 현재 패턴의 문제 분석 ===")
problem_text = "Kotlin을"
word_boundary_pattern = r'\bKotlin\b'
no_boundary_pattern = r'Kotlin'

match1 = re.search(word_boundary_pattern, problem_text)
match2 = re.search(no_boundary_pattern, problem_text)

print(f"텍스트: '{problem_text}'")
print(f"\\b 사용 패턴: {word_boundary_pattern} -> {'매칭' if match1 else '매칭 안됨'}")
print(f"\\b 없는 패턴: {no_boundary_pattern} -> {'매칭' if match2 else '매칭 안됨'}")
