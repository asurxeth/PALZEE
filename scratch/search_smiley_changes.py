import json
import re

log_path = "/Users/pratham/.gemini/antigravity-ide/brain/43c694e0-5643-4328-b7d2-4e82f551337e/.system_generated/logs/transcript.jsonl"
keywords = ["smile_medium", "capture_smile", "smile_small", "onboarding_logo", "onboarding_logo_small", "ic_smiley_avatar"]

try:
    with open(log_path, "r", encoding="utf-8") as f:
        for line in f:
            data = json.loads(line)
            content = data.get("content", "")
            # check tool calls in this step
            tool_calls = data.get("tool_calls", [])
            for tc in tool_calls:
                tc_str = json.dumps(tc)
                if any(kw in tc_str for kw in keywords):
                    print(f"Step {data.get('step_index')}: {tc.get('name')}")
                    args = tc.get("arguments", {})
                    target = args.get("TargetFile") or args.get("Target")
                    print(f"  Target: {target}")
                    desc = args.get("Description", "")
                    if desc:
                        print(f"  Description: {desc}")
                    # Look at replacement content
                    rep = args.get("ReplacementContent", "")
                    for line in rep.splitlines():
                        if any(kw in line for kw in keywords):
                            print(f"    REP: {line.strip()}")
                    print("-" * 40)
except Exception as e:
    print(f"Error: {e}")
