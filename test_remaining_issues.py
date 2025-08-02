# 남은 문제들 디버깅
from src.utils.tech_parser import extract_tech_stacks, ORDERED_TECH_PATTERNS, TECH_PATTERNS
import re

# .NET, C++, C# 테스트
special_char_text = ".NET Core, C++/C#, CI/CD pipelines"
print("=== 특수문자 기술 테스트 ===")
print(f"텍스트: {special_char_text}")
found = extract_tech_stacks(special_char_text)
print(f"발견: {', '.join(found)}")

# 패턴 직접 테스트
print("\n=== 패턴 직접 테스트 ===")
patterns_to_test = {
    '.NET': r'\b(\.NET|dotnet|닷넷|닷넷프레임워크|\.NET Core|\.NET Framework)\b',
    'C++': r'\b(C\+\+|CPP|cpp|C Plus Plus|c\+\+|C＋＋|씨플플|씨플러스플러스|씨쁠쁠|시플플)\b',
    'C#': r'\b(C#|CSharp|csharp|C Sharp|c#|C＃|씨샵|씨샾|C샵|시샵)\b',
}

for tech, pattern in patterns_to_test.items():
    match = re.search(pattern, special_char_text, re.IGNORECASE)
    print(f"{tech}: {'매칭됨' if match else '매칭 안됨'}")

# Spring vs Spring Boot 문제
spring_text = "스프링부트"
print(f"\n=== Spring vs Spring Boot ===")
print(f"텍스트: {spring_text}")
found2 = extract_tech_stacks(spring_text)
print(f"발견: {', '.join(found2)}")

# Ordered patterns 확인
print("\n=== ORDERED_TECH_PATTERNS ===")
for tech, pattern in ORDERED_TECH_PATTERNS[:5]:
    print(f"{tech}: {pattern[:50]}...")
