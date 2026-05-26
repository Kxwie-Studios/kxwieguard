# 🛡️ KxwieGuard

A modern, high-performance bytecode obfuscator for Java 21+ built on the powerful ASM framework. Designed to secure JVM applications, Minecraft mods, and plugins against reverse-engineering.

<p align="left">
  <img src="https://img.shields.io/badge/Java-21%2B-orange?style=flat-square&logo=openjdk&logoColor=white" alt="Java" />
  <img src="https://img.shields.io/badge/Release-v1.0.0-blue?style=flat-square" alt="Release" />
  <img src="https://img.shields.io/badge/License-MIT-red?style=flat-square" alt="License" />
</p>

---

### 🚀 Key Features

* 📦 **Structural Obfuscation**
  * Aggressive class, field, and method renaming.
  * Overloaded signatures and custom dictionary support.
  
* 🔀 **Control Flow Protection**
  * Complex control flow flattening into switch state-machines.
  * Control flow block shuffling to destroy linearity.

* 🔒 **Data & String Cryptography**
  * Dynamic string encryption with anti-tamper security.
  * Polymorphic integer encryption to mask numeric values.

* 🧂 **Instruction Salting**
  * Custom class and method instruction salting to defeat signature matching.

* 🧹 **Metadata Stripping**
  * Full LocalVariableTable (LVT) clearing and LineNumberTable (LNT) mutation.

---

### 📂 Getting Started

Create a directory layout with `workspace/` alongside the jar:

```text
KxwieGuard/
├── kxwieguard.jar
└── workspace/
    ├── config.json
    └── exclusions.json
```

Run the obfuscator from the command line:

```bash
java -jar kxwieguard.jar --config=config.json --exclusions=exclusions.json
```

---

### ⚙️ Configuration (`workspace/config.json`)

```json
{
  "in": "in.jar",
  "out": "out.jar",
  "libs": "libs/",
  "dictionary": "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ",
  "computeFrames": true,
  "watermark": "Protected by KxwieGuard",
  "aggressiveOverload": true,
  "transformers": {
    "trim": { "enabled": true },
    "renameFields": { "enabled": true },
    "renameMethods": { "enabled": true },
    "renameClasses": { "enabled": true },
    "encryptStrings": { "enabled": true },
    "controlFlowFlatten": { "enabled": true }
  }
}
```

---

### ❌ Exclusions (`workspace/exclusions.json`)

```json
{
  "renameClass": {
    "class": [
      "com/example/MyApiClass"
    ]
  }
}
```

---

<p align="left">
  <i>Developed by <b>Kxwie Studios</b></i>
</p>

