import json

log_path = "/Users/pratham/.gemini/antigravity-ide/brain/43c694e0-5643-4328-b7d2-4e82f551337e/.system_generated/logs/transcript.jsonl"
with open(log_path, "r") as f:
    lines = f.readlines()

print(f"Total lines: {len(lines)}")
for i in range(max(0, len(lines) - 50), len(lines)):
    try:
        data = json.loads(lines[i])
        print(f"Step {data.get('step_index')}: {data.get('source')} -> {data.get('type')}")
        if data.get('type') == 'PLANNER_RESPONSE':
            print("  Thinking/Content snippet:", data.get('content', '')[:200])
        elif data.get('type') == 'USER_INPUT':
            print("  User Input:", data.get('content'))
    except Exception as e:
        print(f"Error parsing line {i}: {e}")
