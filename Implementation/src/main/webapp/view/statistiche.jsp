<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>FantaUnisa - Statistiche</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;600;700;800&display=swap" rel="stylesheet">
    <style>
        :root {
            --bg-dark: #1C212E;
            --orange: #F58428;
            --panel-bg: #252B36;
            --text-grey: #bdc3c7;
            --col-p: #e67e22; --col-d: #27ae60; --col-c: #2980b9; --col-a: #c0392b;
        }
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Montserrat', sans-serif; }

        body {
            background-color: var(--bg-dark);
            color: white;
            min-height: 100vh;
            display: flex;
            flex-direction: column;
        }

        .container-stats {
            padding: 40px;
            max-width: 1600px;
            margin: 0 auto;
            width: 100%;
        }

        h2.section-title {
            text-align: center;
            font-weight: 800;
            text-transform: uppercase;
            margin-bottom: 40px;
            font-size: 2rem;
            color: white;
            letter-spacing: 1px;
        }

        .stats-grid {
            display: grid;
            grid-template-columns: 1.5fr 1fr; /* Sinistra più larga */
            gap: 30px;
        }

        .card-stat {
            background: var(--panel-bg);
            border-radius: 20px;
            padding: 25px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.3);
            border: 1px solid rgba(255,255,255,0.05);
        }

        .card-header-title {
            font-size: 1.2rem;
            font-weight: 800;
            text-transform: uppercase;
            margin-bottom: 20px;
            border-bottom: 2px solid rgba(255,255,255,0.1);
            padding-bottom: 15px;
            display: flex;
            align-items: center;
            gap: 10px;
        }

        /* TABELLA CUSTOM */
        .table-custom {
            width: 100%;
            border-collapse: separate;
            border-spacing: 0 8px;
        }
        .table-custom th {
            text-align: left;
            color: var(--text-grey);
            font-size: 0.85rem;
            padding: 0 10px;
            text-transform: uppercase;
        }
        .table-custom td {
            background: rgba(255,255,255,0.03);
            padding: 12px 10px;
            font-size: 0.95rem;
        }
        .table-custom tr td:first-child { border-top-left-radius: 10px; border-bottom-left-radius: 10px; }
        .table-custom tr td:last-child { border-top-right-radius: 10px; border-bottom-right-radius: 10px; }

        .table-custom tr:hover td { background: rgba(255,255,255,0.08); transition: 0.2s; }

        .rank-badge {
            width: 25px; height: 25px; border-radius: 50%;
            display: flex; justify-content: center; align-items: center;
            font-weight: 800; font-size: 0.8rem;
            background: #34495e; color: white;
        }
        .rank-1 { background: #f1c40f; color: #2c3e50; }
        .rank-2 { background: #bdc3c7; color: #2c3e50; }
        .rank-3 { background: #d35400; color: white; }

        .val-highlight { color: var(--orange); font-weight: 800; font-size: 1.1rem; }
        .role-badge {
            padding: 3px 8px; border-radius: 5px; font-size: 0.7rem; font-weight: 800; color: white; display: inline-block; width: 25px; text-align: center;
        }
        .r-p { background: var(--col-p); } .r-d { background: var(--col-d); }
        .r-c { background: var(--col-c); } .r-a { background: var(--col-a); }

        /* BARRA DIFFICOLTA */
        .diff-container {
            display: flex; align-items: center; gap: 10px;
        }
        .diff-bar-bg {
            flex-grow: 1; height: 8px; background: rgba(255,255,255,0.1); border-radius: 4px; overflow: hidden;
        }
        .diff-bar-fill { height: 100%; border-radius: 4px; transition: width 1s ease-out; }
        .val-diff { font-weight: 800; width: 40px; text-align: right; }

    </style>
</head>
<body>

<jsp:include page="../includes/navbar.jsp" />

<div class="container-stats">
    <h2 class="section-title">Centro Statistiche</h2>

    <div class="stats-grid">

        <div class="card-stat">
            <div class="card-header-title">
                <i class="fas fa-trophy" style="color: #f1c40f;"></i> Top Player per Fantamedia
            </div>

            <table class="table-custom">
                <thead>
                <tr>
                    <th width="50">#</th>
                    <th>Giocatore</th>
                    <th>Squadra</th>
                    <th>Ruolo</th>
                    <th style="text-align: right;">FantaMedia</th>
                    <th style="text-align: right;">Media Voto</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="p" items="${topPlayers}" varStatus="loop">
                    <tr>
                        <td>
                            <div class="rank-badge rank-${loop.count}">
                                    ${loop.count}
                            </div>
                        </td>
                        <td style="font-weight: 700;">${p.nome}</td>
                        <td style="color: var(--text-grey); font-size: 0.8rem; text-transform: uppercase;">${p.squadra}</td>
                        <td>
                            <span class="role-badge r-${fn:toLowerCase(p.ruolo)}">${p.ruolo}</span>
                        </td>
                        <td style="text-align: right;" class="val-highlight">
                            <fmt:formatNumber value="${p.fantaMedia}" minFractionDigits="2" maxFractionDigits="2"/>
                        </td>
                        <td style="text-align: right; color: var(--text-grey);">
                            <fmt:formatNumber value="${p.mediaVoto}" minFractionDigits="2" maxFractionDigits="2"/>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>

        <div class="card-stat">
            <div class="card-header-title">
                <i class="fas fa-chart-line" style="color: #3498db;"></i> Indice Performance Squadre
            </div>
            <p style="color: var(--text-grey); font-size: 0.85rem; margin-bottom: 20px;">
                Classifica basata sulla media delle prestazioni aggregate dei giocatori.
            </p>

            <table class="table-custom">
                <thead>
                <tr>
                    <th width="50">#</th>
                    <th>Squadra</th>
                    <th>Indice (0-100)</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="t" items="${teamStats}" varStatus="loop">
                    <tr>
                        <td><span style="font-weight: 700; color: var(--text-grey);">${loop.count}</span></td>
                        <td style="text-transform: uppercase; font-weight: 600;">${t.nomeSquadra}</td>
                        <td>
                            <div class="diff-container">
                                <div class="diff-bar-bg">
                                    <c:choose>
                                        <c:when test="${t.indiceDifficolta >= 80}"><c:set var="barColor" value="#e74c3c"/></c:when>
                                        <c:when test="${t.indiceDifficolta >= 70}"><c:set var="barColor" value="#e67e22"/></c:when>
                                        <c:otherwise><c:set var="barColor" value="#2ecc71"/></c:otherwise>
                                    </c:choose>

                                    <div class="diff-bar-fill"
                                         style="width: ${t.indiceDifficolta}%; background-color: ${barColor};">
                                    </div>
                                </div>
                                <span class="val-diff" style="color: ${barColor};">${t.indiceFormatted}</span>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>

    </div>
</div>

<jsp:include page="../includes/footer.jsp" />

</body>
</html>