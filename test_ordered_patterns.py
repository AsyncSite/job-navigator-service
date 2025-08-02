# ORDERED_TECH_PATTERNS 테스트
from src.utils.tech_parser import ORDERED_TECH_PATTERNS, extract_tech_stacks
import re
import unicodedata

# 전처리 함수 테스트
def test_preprocess(text):
    processed_text = text
    for i in range(len(text) - 1, 0, -1):
        curr_char = text[i]
        prev_char = text[i-1]
        if (prev_char.isalnum() and prev_char.isascii() and 
            unicodedata.category(curr_char).startswith('L') and ord(curr_char) > 127):
            processed_text = processed_text[:i] + ' ' + processed_text[i:]
    return processed_text

test_text = ".NET Core, C++/C#"
processed = test_preprocess(test_text)

print("=== ORDERED_TECH_PATTERNS 디버그 ===")
print(f"원본 텍스트: '{test_text}'")
print(f"전처리된 텍스트: '{processed}'")
print()

# ORDERED_TECH_PATTERNS 직접 테스트
for tech_name, pattern in ORDERED_TECH_PATTERNS:
    if tech_name in ['.NET', 'Spring Boot', 'Spring']:
        match = re.search(pattern, processed, re.IGNORECASE)
        print(f"{tech_name} 패턴: {pattern}")
        print(f"매칭 결과: {'매칭됨 - ' + match.group() if match else '매칭 안됨'}")
        print()

# extract_tech_stacks 전체 결과
print("=== extract_tech_stacks 결과 ===")
result = extract_tech_stacks(test_text)
print(f"발견된 기술: {result}")

# Spring 문제 확인
spring_text = "Java Spring 프레임워크"
spring_result = extract_tech_stacks(spring_text)
print(f"\nSpring 테스트: '{spring_text}' -> {spring_result}")
