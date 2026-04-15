from fastapi import APIRouter, HTTPException
from app.models.schemas import AnalysisRequest
from app.services.analysis_service import AnalysisService

router = APIRouter()
service = AnalysisService()

@router.post("/analyze")
def analyze_project(request: AnalysisRequest):
    try:
        if not request.title or not request.description:
            raise HTTPException(status_code=400, detail="Title and description are required")

        result = service.analyze(
            title=request.title,
            description=request.description,
            deadline=request.deadline
        )
        return result

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/health")
def health_check():
    return {"status": "ok", "service": "Smart Analysis API"}