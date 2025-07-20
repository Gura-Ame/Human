# 🧠 Human

> I'm a student and I'm only free on Sundays.

> A Java-based open-source project that aims to reconstruct human-like intelligence — toward high-performance AI and even AGI.

## 📌 Purpose

**Human** is not just another chatbot or neural net wrapper.  
It is a bold attempt to **mimic the structure of the human mind**, not the behavior of language models.

Unlike current LLM-centric approaches, this project is designed to build artificial intelligence that is:
- modular
- explainable
- goal-driven
- and ultimately, more human-like in structure than statistics.

## 🧰 Technologies

- **Language**: Java 24
- **Design Principles**:
  - No dependency on LLMs or cloud APIs
  - Fully custom natural language understanding pipeline
  - Explicit internal structure: memory, goals, impulses, thoughts
  - Strong emphasis on interpretability and modularity

### Architecture Overview

```
+-------------+     +-------------+     +-------------+
|  Input Text | --> | Interpreter | --> | ResponseGen |
+-------------+     +-------------+     +-------------+
       ↓                  ↓                   ↓
   MemoryModule       GoalModule         OutputModule
```

## 🔍 Project Features

- 🧠 **Simulates Human Mind Elements**:
  - Implements core cognitive artifacts: `Thought`, `Memory`, `Desire`, `Goal`, etc.
- 🔄 **Extensible & Hackable**:
  - Easily inject new modules (e.g. memory systems, reasoning engines)
- 🔏 **No Internet or AI API required**:
  - Works fully offline and can be embedded in standalone systems
- 🧪 **AGI-Oriented Platform**:
  - A playground to experiment with general intelligence, reasoning, and internal mental state models

## 🚀 Getting Started

```bash
# Clone the repo
git clone https://github.com/Gura-Ame/Human.git
cd Human

# Build the project (requires Java 24)
./gradlew build

# Run the main program
./gradlew run
```

## 🗂 Directory Structure (Example)

| Path            | Description                              |
|-----------------|------------------------------------------|
| `src/`          | Source code (core logic and modules)     |
| `Human.java`    | Main entry point                         |
| `core/`         | Human-like abstractions (Thought, etc.)  |
| `brain/`        | Cognitive logic: thinking, decisions     |
| `lang/`         | Natural language understanding            |
| `util/`         | Utilities and internal helpers           |

## 🧭 Philosophy

> "Don't build a chatbot. Build a being."

The Human project is based on the belief that **true intelligence is internal**, and arises from:
- introspective structure
- recursive self-awareness
- dynamic memory and purpose

This is **not** meant to compete with GPT or commercial models.
Instead, it’s an open canvas to experiment with mind-like software design — built from the ground up.

## 🙋 Contribution

I am currently a solo developer on this journey.  
If you’re interested in AGI, cognitive modeling, or human simulation — I’d love your feedback or help.

Open an issue, star the repo, or contact me directly.

## 🧠 Built by

**Gura Ame**  
https://github.com/Gura-Ame

---

> ⚠️ Disclaimer: This project is experimental and under active development.  
> Its goal is to explore new directions, not to replace or replicate current AI models.
