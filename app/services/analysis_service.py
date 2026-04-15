import re
from datetime import datetime, date
from typing import List, Dict, Any
from app.data.skills_db import SKILLS_DB, COMPLEXITY_KEYWORDS, RISK_KEYWORDS


class AnalysisService:

    # ─────────────────────────────────────────
    # 1. DÉTECTION DES SKILLS
    # ─────────────────────────────────────────
    def detect_skills(self, text: str) -> List[Dict[str, Any]]:
        text_lower = text.lower()
        detected = []
        seen = set()

        # Trier par longueur décroissante pour matcher "spring boot" avant "spring"
        sorted_skills = sorted(SKILLS_DB.keys(), key=len, reverse=True)

        for skill in sorted_skills:
            if skill in seen:
                continue
            pattern = r'\b' + re.escape(skill) + r'\b'
            if re.search(pattern, text_lower):
                detected.append({
                    "name": skill.title(),
                    "category": SKILLS_DB[skill]["category"],
                    "hourly_rate": SKILLS_DB[skill]["hourly_rate"],
                    "demand": SKILLS_DB[skill]["demand"]
                })
                seen.add(skill)

        return detected

    # ─────────────────────────────────────────
    # 2. COMPLEXITÉ DU PROJET
    # ─────────────────────────────────────────
    def detect_complexity(self, text: str, skills: List[Dict]) -> Dict[str, Any]:
        text_lower = text.lower()
        score = 0

        # Score basé sur les mots-clés
        for keyword in COMPLEXITY_KEYWORDS["enterprise"]:
            if keyword in text_lower:
                score += 4
        for keyword in COMPLEXITY_KEYWORDS["complex"]:
            if keyword in text_lower:
                score += 3
        for keyword in COMPLEXITY_KEYWORDS["medium"]:
            if keyword in text_lower:
                score += 2
        for keyword in COMPLEXITY_KEYWORDS["simple"]:
            if keyword in text_lower:
                score -= 1

        # Score basé sur le nombre de skills
        num_skills = len(skills)
        if num_skills >= 8:
            score += 4
        elif num_skills >= 5:
            score += 3
        elif num_skills >= 3:
            score += 2
        else:
            score += 1

        # Score basé sur les catégories uniques
        categories = set(s["category"] for s in skills)
        score += len(categories)

        # Déterminer le niveau
        if score >= 12:
            level = "Enterprise"
            multiplier = 2.5
        elif score >= 8:
            level = "Complex"
            multiplier = 1.8
        elif score >= 4:
            level = "Medium"
            multiplier = 1.2
        else:
            level = "Simple"
            multiplier = 0.8

        return {
            "level": level,
            "score": score,
            "multiplier": multiplier
        }

    # ─────────────────────────────────────────
    # 3. DURÉE ESTIMÉE
    # ─────────────────────────────────────────
    def estimate_duration(self, complexity: Dict, skills: List[Dict], deadline: str = None) -> Dict[str, Any]:
        base_weeks = {
            "Simple": 2,
            "Medium": 6,
            "Complex": 12,
            "Enterprise": 24
        }

        weeks = base_weeks.get(complexity["level"], 6)

        # Ajustement selon le nombre de skills
        weeks += len(skills) * 0.3

        weeks = round(weeks)

        # Vérification deadline
        warning = None
        if deadline:
            try:
                deadline_date = datetime.strptime(deadline, "%Y-%m-%d").date()
                days_available = (deadline_date - date.today()).days
                weeks_available = days_available / 7
                if weeks_available < weeks:
                    warning = f"⚠️ Deadline tight! Project needs ~{weeks} weeks but only {round(weeks_available)} weeks available."
            except:
                pass

        return {
            "min_weeks": max(1, weeks - 1),
            "max_weeks": weeks + 2,
            "estimated_weeks": weeks,
            "warning": warning
        }

    # ─────────────────────────────────────────
    # 4. BUDGET ESTIMÉ
    # ─────────────────────────────────────────
    def estimate_budget(self, skills: List[Dict], complexity: Dict, duration: Dict) -> Dict[str, Any]:
        if not skills:
            base_rate = 40
        else:
            base_rate = sum(s["hourly_rate"] for s in skills) / len(skills)

        hours_per_week = 40
        weeks = duration["estimated_weeks"]
        multiplier = complexity["multiplier"]

        base_budget = base_rate * hours_per_week * weeks * multiplier

        return {
            "min": round(base_budget * 0.8),
            "max": round(base_budget * 1.3),
            "recommended": round(base_budget),
            "currency": "USD",
            "hourly_rate_avg": round(base_rate)
        }

    # ─────────────────────────────────────────
    # 5. PROFIT ESTIMÉ
    # ─────────────────────────────────────────
    def estimate_profit(self, budget: Dict) -> Dict[str, Any]:
        platform_fee = 0.10        # 10% frais plateforme
        freelancer_margin = 0.20   # 20% marge freelancer

        recommended = budget["recommended"]
        platform_revenue = round(recommended * platform_fee)
        freelancer_profit = round(recommended * freelancer_margin)
        net_project_cost = round(recommended * (1 - platform_fee))

        return {
            "platform_fee_percent": int(platform_fee * 100),
            "platform_revenue": platform_revenue,
            "freelancer_margin_percent": int(freelancer_margin * 100),
            "freelancer_profit": freelancer_profit,
            "net_project_cost": net_project_cost
        }

    # ─────────────────────────────────────────
    # 6. RISK SCORE
    # ─────────────────────────────────────────
    def calculate_risk(self, text: str, skills: List[Dict], duration: Dict, deadline: str = None) -> Dict[str, Any]:
        text_lower = text.lower()
        risk_score = 0
        risk_factors = []

        # Facteurs basés sur les mots-clés
        for keyword in RISK_KEYWORDS["high"]:
            if keyword in text_lower:
                risk_score += 3
                risk_factors.append(f"High-risk keyword: '{keyword}'")

        for keyword in RISK_KEYWORDS["medium"]:
            if keyword in text_lower:
                risk_score += 1

        # Longueur description
        if len(text) < 100:
            risk_score += 3
            risk_factors.append("Description too short — unclear requirements")
        elif len(text) < 200:
            risk_score += 1
            risk_factors.append("Description could be more detailed")

        # Deadline serrée
        if deadline:
            try:
                deadline_date = datetime.strptime(deadline, "%Y-%m-%d").date()
                days_left = (deadline_date - date.today()).days
                if days_left < 14:
                    risk_score += 4
                    risk_factors.append("Very tight deadline (< 2 weeks)")
                elif days_left < 30:
                    risk_score += 2
                    risk_factors.append("Short deadline (< 1 month)")
            except:
                pass

        # Trop de skills rares
        rare_skills = [s for s in skills if s["demand"] == "low"]
        if len(rare_skills) >= 2:
            risk_score += 2
            risk_factors.append(f"{len(rare_skills)} rare skills required")

        # Déterminer le niveau
        if risk_score >= 8:
            level = "High"
            color = "#dc2626"
            advice = "Consider breaking this project into smaller milestones."
        elif risk_score >= 4:
            level = "Medium"
            color = "#d97706"
            advice = "Define clear requirements and set intermediate checkpoints."
        else:
            level = "Low"
            color = "#16a34a"
            advice = "This project has clear scope and manageable complexity."

        return {
            "level": level,
            "score": min(risk_score, 10),
            "max_score": 10,
            "color": color,
            "factors": risk_factors[:5],  # max 5 facteurs
            "advice": advice
        }

    # ─────────────────────────────────────────
    # 7. FREELANCERS COMPATIBLES
    # ─────────────────────────────────────────
    def estimate_freelancers(self, skills: List[Dict], complexity: Dict) -> Dict[str, Any]:
        # Base selon la complexité
        base_count = {
            "Simple": 150,
            "Medium": 80,
            "Complex": 35,
            "Enterprise": 12
        }

        count = base_count.get(complexity["level"], 50)

        # Réduction selon skills rares
        rare_count = sum(1 for s in skills if s["demand"] == "low")
        medium_count = sum(1 for s in skills if s["demand"] == "medium")

        count -= rare_count * 15
        count -= medium_count * 5
        count = max(3, count)

        # Catégories nécessaires
        categories = list(set(s["category"] for s in skills))

        return {
            "estimated_count": count,
            "range": f"{max(1, count - 10)} - {count + 20}",
            "required_categories": categories,
            "availability": "High" if count > 50 else "Medium" if count > 20 else "Low"
        }

    # ─────────────────────────────────────────
    # ANALYSE COMPLÈTE
    # ─────────────────────────────────────────
    def analyze(self, title: str, description: str, deadline: str = None) -> Dict[str, Any]:
        full_text = f"{title} {description}"

        skills       = self.detect_skills(full_text)
        complexity   = self.detect_complexity(full_text, skills)
        duration     = self.estimate_duration(complexity, skills, deadline)
        budget       = self.estimate_budget(skills, complexity, duration)
        profit       = self.estimate_profit(budget)
        risk         = self.calculate_risk(full_text, skills, duration, deadline)
        freelancers  = self.estimate_freelancers(skills, complexity)

        return {
            "skills":       skills,
            "complexity":   complexity,
            "duration":     duration,
            "budget":       budget,
            "profit":       profit,
            "risk":         risk,
            "freelancers":  freelancers
        }