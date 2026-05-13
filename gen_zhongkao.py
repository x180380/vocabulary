#!/usr/bin/env python3
"""Generate zhongkao_words.json from zhongkao_words.txt using Claude API."""

import json
import os
import re
import sys
import time
import anthropic

TXT_PATH = "app/src/main/assets/seed/zhongkao_words.txt"
JSON_PATH = "app/src/main/assets/seed/zhongkao_words.json"
BATCH_SIZE = 30
ID_BASE = 3001

def read_words():
    words = []
    with open(TXT_PATH) as f:
        for line in f:
            word = line.strip()
            if word:
                words.append(word)
    return words

def generate_batch(client, words, id_start):
    word_list = "\n".join(f"{i+1}. {w}" for i, w in enumerate(words))
    prompt = f"""Generate JSON data for these {len(words)} English words for Chinese middle school students (zhongkao level).

Words:
{word_list}

Return a JSON array with exactly {len(words)} objects in the same order as the words listed. Each object must have:
- "id": integer starting from {id_start}
- "english": the word exactly as given
- "phonetics": {{"british": "IPA", "american": "IPA"}}
- "definitions": array of {{"partOfSpeech": "n./v./adj./adv./prep./conj./pron./interj./num.", "meaning": "Chinese meaning"}}
- "examples": array with exactly 1 object {{"english": "simple example sentence", "chinese": "Chinese translation"}}
- "isBookmarked": false

Rules:
- Use accurate IPA phonetics
- Chinese meanings should be concise and appropriate for middle school level
- Example sentences should be simple and educational
- Include all common parts of speech if a word has multiple

Return ONLY the JSON array, no other text."""

    response = client.messages.create(
        model="claude-sonnet-4-6",
        max_tokens=8000,
        messages=[{"role": "user", "content": prompt}]
    )

    text = response.content[0].text.strip()
    # Extract JSON array if wrapped in markdown
    match = re.search(r'\[[\s\S]*\]', text)
    if match:
        text = match.group(0)

    return json.loads(text)

def main():
    client = anthropic.Anthropic()
    words = read_words()
    print(f"Loaded {len(words)} words from {TXT_PATH}")

    # Load existing progress if any
    existing = []
    if os.path.exists(JSON_PATH):
        try:
            with open(JSON_PATH) as f:
                content = f.read().strip()
                if content and content != "[]":
                    existing = json.loads(content)
                    print(f"Resuming from {len(existing)} existing entries")
        except Exception:
            pass

    existing_words = {e["english"] for e in existing}
    all_entries = list(existing)

    # Find words still needing processing
    pending = [(i, w) for i, w in enumerate(words) if w not in existing_words]
    print(f"Words remaining: {len(pending)}")

    if not pending:
        print("All words already generated!")
        return

    # Process in batches
    for batch_start in range(0, len(pending), BATCH_SIZE):
        batch = pending[batch_start:batch_start + BATCH_SIZE]
        indices = [idx for idx, _ in batch]
        batch_words = [w for _, w in batch]
        id_start = ID_BASE + indices[0]

        print(f"Processing batch {batch_start//BATCH_SIZE + 1}/{(len(pending)+BATCH_SIZE-1)//BATCH_SIZE}: "
              f"words {indices[0]+1}-{indices[-1]+1} (IDs {id_start}-{id_start+len(batch)-1})")

        retries = 3
        for attempt in range(retries):
            try:
                entries = generate_batch(client, batch_words, id_start)
                # Fix IDs to ensure correct ordering
                for j, entry in enumerate(entries):
                    entry["id"] = ID_BASE + indices[j]
                    entry["english"] = batch_words[j]  # ensure exact word
                all_entries.extend(entries)
                break
            except Exception as e:
                print(f"  Attempt {attempt+1} failed: {e}")
                if attempt < retries - 1:
                    time.sleep(2)
                else:
                    print(f"  Skipping batch after {retries} failures")

        # Sort by ID and save after each batch
        all_entries.sort(key=lambda e: e["id"])
        with open(JSON_PATH, "w", encoding="utf-8") as f:
            json.dump(all_entries, f, ensure_ascii=False, indent=4)

        print(f"  Saved {len(all_entries)} total entries")
        time.sleep(0.5)  # small delay between batches

    print(f"\nDone! Generated {len(all_entries)} entries in {JSON_PATH}")

if __name__ == "__main__":
    main()
