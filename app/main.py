from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List
from app.routers import analysis
from groq import Groq
import json
import re
import os

app = FastAPI(
    title="Smart Analysis API",
    description="AI-powered project analysis for FreeLancy",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:8085", "http://localhost:4200"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(analysis.router, prefix="/api", tags=["analysis"])

@app.get("/")
def root():
    return {"message": "Smart Analysis API is running", "version": "1.0.0"}

# ── Modèles ───────────────────────────────────────────
class ChatMessageItem(BaseModel):
    senderName: str
    senderRole: str
    content: str

class AnalyzeRequest(BaseModel):
    projectTitle: str
    messages: List[ChatMessageItem]

# ── Endpoint analyse discussion ───────────────────────
@app.post("/analyze-discussion")
async def analyze_discussion(req: AnalyzeRequest):
    if not req.messages:
        return {
            "summary": "No messages yet in this discussion.",
            "phase": "ETUDE",
            "phase_reason": "No discussion started yet.",
            "progress_percent": 0,
            "key_points": []
        }

    conversation = "\n".join([
        f"{m.senderRole} ({m.senderName}): {m.content}"
        for m in req.messages
    ])

    prompt = f"""You are analyzing a discussion between a client and a freelancer about a software project.

Project title: {req.projectTitle}

Discussion:
{conversation}

Analyze this discussion and respond ONLY with a valid JSON object, no extra text, no markdown:
{{
    "summary": "Summary of the discussion in 2-3 sentences",
    "phase": "ETUDE",
    "phase_reason": "Why this phase in 1 sentence",
    "progress_percent": 10,
    "key_points": ["point 1", "point 2", "point 3"]
}}

Rules for phase detection:
- ETUDE: discussing requirements, features, planning, wireframes, budget, timeline
- DEVELOPPEMENT: discussing code, implementation, bugs, features in progress, API
- TEST: discussing testing, validation, bug fixes, QA, review
- DEPLOIEMENT: discussing deployment, production, delivery, launch, hosting

Choose the phase that best matches the discussion content.
progress_percent must be a number: ETUDE=10-25, DEVELOPPEMENT=25-70, TEST=70-90, DEPLOIEMENT=90-100"""

    # ── Clé API dans variable d'environnement (plus sécurisé) ──
    api_key = os.environ.get("GROQ_API_KEY", "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")

    client = Groq(api_key=api_key)

    response = client.chat.completions.create(
        model="llama-3.3-70b-versatile",
        max_tokens=600,
        messages=[{"role": "user", "content": prompt}]
    )

    raw = response.choices[0].message.content.strip()
    raw = re.sub(r'```json|```', '', raw).strip()

    try:
        result = json.loads(raw)
    except json.JSONDecodeError:
        result = {
            "summary": raw[:200],
            "phase": "ETUDE",
            "phase_reason": "Could not parse AI response.",
            "progress_percent": 10,
            "key_points": []
        }

    return result