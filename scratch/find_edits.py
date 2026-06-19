import json

log_path = "/Users/pratham/.gemini/antigravity-ide/brain/43c694e0-5643-4328-b7d2-4e82f551337e/.system_generated/logs/transcript.jsonl"
try:
    with open(log_path, "r", encoding="utf-8") as f:
        for line in f:
            data = json.loads(line)
            tool_calls = data.get("tool_calls", [])
            for tc in tool_calls:
                name = tc.get("name")
                if name in ["replace_file_content", "multi_replace_file_content"]:
                    args = tc.get("arguments", {})
                    target = args.get("TargetFile", "")
                    desc = args.get("Description", "")
                    instr = args.get("Instruction", "")
                    print(f"Step {data.get('step_index')}: {name} on {target}")
                    print(f"  Description: {desc}")
                    print(f"  Instruction: {instr}")
except Exception as e:
    print(f"Error: {e}")
