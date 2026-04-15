from pydantic import BaseModel
from typing import Optional

class AnalysisRequest(BaseModel):
    title: str
    description: str
    deadline: Optional[str] = None  # format: YYYY-MM-DD

class AnalysisResponse(BaseModel):
    skills: list
    complexity: dict
    duration: dict
    budget: dict
    profit: dict
    risk: dict
    freelancers: dict