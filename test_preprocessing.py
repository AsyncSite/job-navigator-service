# 전처리 과정 테스트
import re
import unicodedata

def preprocess_text(text):
    """Test preprocessing logic"""
    processed_text = text
    for i in range(len(text) - 1, 0, -1):
        curr_char = text[i]
        prev_char = text[i-1]
        # Check if previous is English/number and current is Korean
        if (prev_char.isalnum() and prev_char.isascii() and 
            unicodedata.category(curr_char).startswith('L') and ord(curr_char) > 127):
            processed_text = processed_text[:i] + ' ' + processed_text[i:]
    return processed_text

# 테스트
test_cases = [
    ".NET Core, C++/C#",
    "Kotlin을 사용",
    "스프링부트",
]

print("=== 전처리 테스트 ===")
for text in test_cases:
    processed = preprocess_text(text)
    print(f"원본: '{text}'")
    print(f"처리: '{processed}'")
    print()

# .NET 패턴 테스트
print("=== .NET 패턴 테스트 ===")
dotnet_pattern = r'(\.NET|dotnet|닷넷)'
test_texts = [".NET", ".NET Core", "dotnet", "use .NET here"]

for text in test_texts:
    match = re.search(dotnet_pattern, text, re.IGNORECASE)
    print(f"'{text}' -> {'매칭' if match else '매칭 안됨'}")
