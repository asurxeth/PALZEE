with open("./app/src/main/java/com/finrein/pals/presentation/home/HomeScreen.kt", "r") as f:
    lines = f.readlines()

brace_count = 0
for idx, line in enumerate(lines):
    line_num = idx + 1
    
    cleaned_line = ""
    in_string = False
    i = 0
    while i < len(line):
        if line[i:i+2] == "//":
            break
        elif line[i] == '"' and (i == 0 or line[i-1] != '\\'):
            in_string = not in_string
        elif not in_string:
            cleaned_line += line[i]
        i += 1
        
    for char in cleaned_line:
        if char == '{':
            brace_count += 1
        elif char == '}':
            brace_count -= 1

print(f"Total unmatched braces at the end of file: {brace_count}")
