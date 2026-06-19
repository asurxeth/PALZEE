import json

log_path = "/Users/pratham/.gemini/antigravity-ide/brain/43c694e0-5643-4328-b7d2-4e82f551337e/.system_generated/logs/transcript.jsonl"
try:
    with open(log_path, "r", encoding="utf-8") as f:
        for line in f:
            if "fun CapturedPreviewScreen" in line and "BoxWithConstraints" not in line:
                data = json.loads(line)
                content = data.get("content", "")
                print(f"Step {data.get('step_index')}:")
                # find where CapturedPreviewScreen layout is
                idx = content.find("fun CapturedPreviewScreen")
                if idx != -1:
                    print(content[idx:idx+1500])
                print("-" * 50)
except Exception as e:
    print(f"Error: {e}")
