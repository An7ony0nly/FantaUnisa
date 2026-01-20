<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>FantaUnisa</title>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;600;700;800;900&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        :root {
            --bg-dark: #1C212E;
            --orange: #F58428;
            --orange-hover: #d35400;
            --accent-blue: #365B95;
            --text-grey: #bdc3c7;
            --card-bg: #252B36;
        }

        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
            font-family: 'Montserrat', sans-serif;
        }

        body {
            background-color: var(--bg-dark);
            color: white;
            overflow-x: hidden;
        }

        /* --- HERO SECTION --- */
        .hero {
            min-height: 70vh;
            display: flex;
            align-items: center;
            padding: 0 10%;
            position: relative;
        }

        /* Background decorations */
        .blob {
            position: absolute;
            border-radius: 50%;
            filter: blur(100px);
            z-index: -1;
            opacity: 0.2;
        }
        .b1 { width: 400px; height: 400px; background: var(--orange); top: -100px; right: -100px; }
        .b2 { width: 300px; height: 300px; background: var(--accent-blue); bottom: 50px; left: -100px; }

        .hero-content {
            flex: 1;
            max-width: 600px;
            z-index: 2;
        }

        .badge-new {
            background: rgba(245, 132, 40, 0.15);
            color: var(--orange);
            padding: 8px 15px;
            border-radius: 20px;
            font-weight: 700;
            font-size: 0.8rem;
            display: inline-block;
            margin-bottom: 20px;
            border: 1px solid rgba(245, 132, 40, 0.3);
        }

        .hero h1 {
            font-size: 3.5rem;
            line-height: 1.1;
            margin-bottom: 20px;
            font-weight: 900;
        }

        .hero h1 span {
            color: var(--orange);
        }

        .hero p {
            font-size: 1.1rem;
            color: var(--text-grey);
            margin-bottom: 40px;
            line-height: 1.6;
        }

        .cta-group {
            display: flex;
            gap: 15px;
        }

        .btn-large {
            padding: 15px 35px;
            border-radius: 12px;
            font-weight: 800;
            text-decoration: none;
            font-size: 1rem;
            transition: transform 0.2s;
        }

        .btn-primary {
            background: var(--orange);
            color: white;
            box-shadow: 0 10px 25px rgba(245, 132, 40, 0.4);
        }

        .btn-primary:hover { transform: translateY(-3px); }

        .btn-outline {
            border: 2px solid rgba(255,255,255,0.2);
            color: white;
        }

        .btn-outline:hover {
            border-color: white;
            background: rgba(255,255,255,0.05);
        }

        .hero-visual {
            flex: 1;
            display: flex;
            justify-content: center;
            position: relative;
        }

        /* Mockup telefono stilizzato con CSS */
        .phone-mockup {
            width: 300px;
            height: 600px;
            background: var(--bg-dark);
            border: 10px solid #2c3e50;
            border-radius: 40px;
            position: relative;
            box-shadow: 0 20px 50px rgba(0,0,0,0.5);
            overflow: hidden;
        }
        .phone-mockup::before {
            content: '';
            position: absolute;
            top: 0; left: 50%;
            transform: translateX(-50%);
            width: 120px; height: 25px;
            background: #2c3e50;
            border-bottom-left-radius: 15px;
            border-bottom-right-radius: 15px;
            z-index: 10;
        }
        /* Contenuto finto dello schermo */
        .screen-content {
            padding: 40px 20px;
            background: linear-gradient(180deg, #252B36 0%, #1C212E 100%);
            height: 100%;
            display: flex;
            flex-direction: column;
            gap: 15px;
        }
        .fake-card {
            background: rgba(255,255,255,0.05);
            border-radius: 15px;
            height: 80px;
            width: 100%;
        }
        .fake-pitch {
            background: var(--orange);
            flex: 1;
            border-radius: 15px;
            opacity: 0.8;
            position: relative;
        }
        .fake-player-dot {
            width: 30px; height: 30px;
            background: white; border-radius: 50%;
            position: absolute;
            top: 50%; left: 50%; transform: translate(-50%, -50%);
            box-shadow: 0 0 10px rgba(0,0,0,0.3);
        }

        /* --- FEATURES SECTION --- */
        .features {
            padding: 100px 10%;
            background: #151922;
        }

        .section-header {
            text-align: center;
            margin-bottom: 60px;
        }

        .section-header h2 {
            font-size: 2.5rem;
            font-weight: 800;
            margin-bottom: 15px;
        }
        .section-header p {
            color: var(--text-grey);
            max-width: 600px;
            margin: 0 auto;
        }

        .grid-cards {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 30px;
        }

        .feature-card {
            background: var(--card-bg);
            padding: 40px 30px;
            border-radius: 25px;
            transition: transform 0.3s, box-shadow 0.3s;
            border: 1px solid rgba(255,255,255,0.05);
        }

        .feature-card:hover {
            transform: translateY(-10px);
            box-shadow: 0 10px 30px rgba(0,0,0,0.3);
            border-color: var(--orange);
        }

        .icon-box {
            width: 60px;
            height: 60px;
            background: rgba(245, 132, 40, 0.1);
            color: var(--orange);
            border-radius: 15px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.5rem;
            margin-bottom: 25px;
        }

        .feature-card h3 {
            font-size: 1.5rem;
            font-weight: 700;
            margin-bottom: 15px;
        }

        .feature-card p {
            color: var(--text-grey);
            line-height: 1.6;
            font-size: 0.95rem;
        }

        /* Responsive */
        @media (max-width: 900px) {
            .hero { flex-direction: column; text-align: center; padding-top: 100px; }
            .hero-content { margin-bottom: 60px; }
            .cta-group { justify-content: center; }
            .footer-content { grid-template-columns: 1fr; text-align: center; gap: 30px; }
            .footer-desc { margin: 0 auto; }
        }
    </style>
</head>
<body>
<jsp:include page="../includes/navbar.jsp" />
<!-- HERO SECTION -->
<section class="hero">
    <div class="blob b1"></div>
    <div class="blob b2"></div>

    <div class="hero-content">
        <div class="badge-new">Edizione 2026/2027</div>
        <h1>Il tuo tool preferito <br><span>tutte le tue formazioni</span></h1>
        <p>Crea la tua rosa, confrontati con i tuoi amici e metti alla prova le tue previsioni!</p>

        <div class="cta-group">
            <a href="${pageContext.request.contextPath}/view/login.jsp" class="btn-large btn-primary">Inizia Ora</a>
            <a href="#" class="btn-large btn-outline">Scopri di più</a>
        </div>
    </div>

    <div class="hero-visual">
        <div class="phone-mockup">
            <div class="screen-content">
                <div style="font-weight:800; font-size:1.2rem; text-align:center; margin-bottom:10px;">La tua Rosa</div>
                <div class="fake-card"></div>
                <div class="fake-card"></div>
                <div class="fake-pitch">
                    <div class="fake-player-dot"></div>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- FEATURES -->
<section class="features">
    <div class="section-header">
        <h2>Tutto ciò che ti serve</h2>
        <p>Una piattaforma completa pensata per aiutarti nella scelta della formazione.</p>
    </div>

    <div class="grid-cards">
        <!-- Card 2 -->
        <div class="feature-card">
            <div class="icon-box"><i class="fas fa-tshirt"></i></div>
            <h3>Gestione Rosa</h3>
            <p>Inserisci la formazione, confrontati con la community.</p>
        </div>

        <!-- Card 3 -->
        <div class="feature-card">
            <div class="icon-box"><i class="fas fa-trophy"></i></div>
            <h3>Classifiche</h3>
            <p>Competi con gli altri utenti e diventa il più apprezzato.</p>
        </div>
    </div>
</section>
<jsp:include page="../includes/footer.jsp" />


</body>
</html>

<!---->