# Base de données des skills avec leur catégorie et taux horaire moyen
SKILLS_DB = {
    # Frontend
    "angular":      {"category": "Frontend",  "hourly_rate": 45, "demand": "high"},
    "react":        {"category": "Frontend",  "hourly_rate": 50, "demand": "high"},
    "vue":          {"category": "Frontend",  "hourly_rate": 42, "demand": "medium"},
    "html":         {"category": "Frontend",  "hourly_rate": 25, "demand": "high"},
    "css":          {"category": "Frontend",  "hourly_rate": 25, "demand": "high"},
    "javascript":   {"category": "Frontend",  "hourly_rate": 45, "demand": "high"},
    "typescript":   {"category": "Frontend",  "hourly_rate": 48, "demand": "high"},
    "tailwind":     {"category": "Frontend",  "hourly_rate": 38, "demand": "medium"},
    "bootstrap":    {"category": "Frontend",  "hourly_rate": 30, "demand": "medium"},

    # Backend
    "java":         {"category": "Backend",   "hourly_rate": 55, "demand": "high"},
    "spring":       {"category": "Backend",   "hourly_rate": 58, "demand": "high"},
    "spring boot":  {"category": "Backend",   "hourly_rate": 58, "demand": "high"},
    "python":       {"category": "Backend",   "hourly_rate": 52, "demand": "high"},
    "django":       {"category": "Backend",   "hourly_rate": 50, "demand": "medium"},
    "fastapi":      {"category": "Backend",   "hourly_rate": 52, "demand": "medium"},
    "flask":        {"category": "Backend",   "hourly_rate": 45, "demand": "medium"},
    "node":         {"category": "Backend",   "hourly_rate": 50, "demand": "high"},
    "nodejs":       {"category": "Backend",   "hourly_rate": 50, "demand": "high"},
    "express":      {"category": "Backend",   "hourly_rate": 47, "demand": "medium"},
    "php":          {"category": "Backend",   "hourly_rate": 38, "demand": "medium"},
    "laravel":      {"category": "Backend",   "hourly_rate": 42, "demand": "medium"},
    "ruby":         {"category": "Backend",   "hourly_rate": 50, "demand": "low"},
    "golang":       {"category": "Backend",   "hourly_rate": 65, "demand": "medium"},
    "go":           {"category": "Backend",   "hourly_rate": 65, "demand": "medium"},
    "c#":           {"category": "Backend",   "hourly_rate": 55, "demand": "medium"},
    ".net":         {"category": "Backend",   "hourly_rate": 55, "demand": "medium"},
    "dotnet":       {"category": "Backend",   "hourly_rate": 55, "demand": "medium"},

    # Mobile
    "flutter":      {"category": "Mobile",    "hourly_rate": 55, "demand": "high"},
    "dart":         {"category": "Mobile",    "hourly_rate": 50, "demand": "medium"},
    "swift":        {"category": "Mobile",    "hourly_rate": 65, "demand": "medium"},
    "kotlin":       {"category": "Mobile",    "hourly_rate": 60, "demand": "medium"},
    "react native": {"category": "Mobile",    "hourly_rate": 55, "demand": "high"},
    "android":      {"category": "Mobile",    "hourly_rate": 55, "demand": "medium"},
    "ios":          {"category": "Mobile",    "hourly_rate": 65, "demand": "medium"},

    # Database
    "mysql":        {"category": "Database",  "hourly_rate": 40, "demand": "high"},
    "postgresql":   {"category": "Database",  "hourly_rate": 45, "demand": "high"},
    "postgres":     {"category": "Database",  "hourly_rate": 45, "demand": "high"},
    "mongodb":      {"category": "Database",  "hourly_rate": 45, "demand": "high"},
    "redis":        {"category": "Database",  "hourly_rate": 48, "demand": "medium"},
    "oracle":       {"category": "Database",  "hourly_rate": 55, "demand": "medium"},
    "sqlite":       {"category": "Database",  "hourly_rate": 35, "demand": "medium"},
    "firebase":     {"category": "Database",  "hourly_rate": 42, "demand": "medium"},

    # DevOps / Cloud
    "docker":       {"category": "DevOps",    "hourly_rate": 60, "demand": "high"},
    "kubernetes":   {"category": "DevOps",    "hourly_rate": 75, "demand": "medium"},
    "aws":          {"category": "Cloud",     "hourly_rate": 70, "demand": "high"},
    "azure":        {"category": "Cloud",     "hourly_rate": 68, "demand": "high"},
    "gcp":          {"category": "Cloud",     "hourly_rate": 68, "demand": "medium"},
    "linux":        {"category": "DevOps",    "hourly_rate": 50, "demand": "high"},
    "git":          {"category": "DevOps",    "hourly_rate": 35, "demand": "high"},
    "ci/cd":        {"category": "DevOps",    "hourly_rate": 65, "demand": "medium"},

    # Data / AI
    "machine learning": {"category": "AI/ML", "hourly_rate": 80, "demand": "high"},
    "deep learning":    {"category": "AI/ML", "hourly_rate": 90, "demand": "medium"},
    "tensorflow":       {"category": "AI/ML", "hourly_rate": 85, "demand": "medium"},
    "pytorch":          {"category": "AI/ML", "hourly_rate": 85, "demand": "medium"},
    "nlp":              {"category": "AI/ML", "hourly_rate": 85, "demand": "medium"},
    "pandas":           {"category": "Data",  "hourly_rate": 55, "demand": "medium"},
    "numpy":            {"category": "Data",  "hourly_rate": 50, "demand": "medium"},
    "data science":     {"category": "Data",  "hourly_rate": 70, "demand": "high"},
    "power bi":         {"category": "Data",  "hourly_rate": 55, "demand": "medium"},
    "tableau":          {"category": "Data",  "hourly_rate": 60, "demand": "medium"},

    # Architecture
    "rest api":     {"category": "Architecture", "hourly_rate": 55, "demand": "high"},
    "restful":      {"category": "Architecture", "hourly_rate": 55, "demand": "high"},
    "graphql":      {"category": "Architecture", "hourly_rate": 60, "demand": "medium"},
    "microservices":{"category": "Architecture", "hourly_rate": 70, "demand": "medium"},
    "mvc":          {"category": "Architecture", "hourly_rate": 45, "demand": "medium"},
    "api":          {"category": "Architecture", "hourly_rate": 50, "demand": "high"},

    # Design
    "figma":        {"category": "Design",    "hourly_rate": 45, "demand": "high"},
    "ui/ux":        {"category": "Design",    "hourly_rate": 50, "demand": "high"},
    "photoshop":    {"category": "Design",    "hourly_rate": 40, "demand": "medium"},
}

# Mots-clés de complexité
COMPLEXITY_KEYWORDS = {
    "simple":   ["simple", "basic", "small", "landing page", "static", "brochure", "portfolio"],
    "medium":   ["dashboard", "crud", "admin", "ecommerce", "blog", "cms", "api", "mobile app"],
    "complex":  ["microservices", "real-time", "machine learning", "ai", "payment", "scalable", "enterprise"],
    "enterprise": ["blockchain", "big data", "distributed", "high performance", "millions of users", "saas platform"]
}

# Mots-clés de risque
RISK_KEYWORDS = {
    "high":   ["urgent", "asap", "immediately", "complex", "blockchain", "ai", "real-time", "migration"],
    "medium": ["integrate", "custom", "optimize", "refactor", "upgrade", "multiple"],
    "low":    ["simple", "basic", "standard", "template", "existing", "small"]
}