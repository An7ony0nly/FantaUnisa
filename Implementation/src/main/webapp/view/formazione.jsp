<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="it">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>FantaUnisa - Formazione</title>
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
  <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;600;700;800&display=swap" rel="stylesheet">
  <style>
    :root {
      --bg-dark: #1C212E;
      --orange: #F58428;
      --panel-bg: #252B36;
      --text-grey: #bdc3c7;
    }
    * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Montserrat', sans-serif; }

    body {
      background-color: var(--bg-dark);
      color: white;
      min-height: 100vh;
      display: flex;
      flex-direction: column;
    }

    .menu a { text-decoration: none; color: white; padding: 8px 20px; font-size: 0.8rem; font-weight: 600; border-radius: 20px; }
    .menu a.active { background: white; color: var(--bg-dark); font-weight: 800; }

    /* Campo formazione */
    .layout {
      margin: 30px;
      display: grid;
      grid-template-columns: 250px 1fr 280px; /* Dettagli */
      gap: 20px;
      padding: 20px 40px;
      height: calc(100vh - 80px);
    }

    /* Panchina */
    .sidebar-list { overflow-y: auto; padding-right: 10px; }
    .list-header { background: #E3F6FA; color: var(--bg-dark); padding: 10px; border-radius: 10px; text-align: center; font-weight: 800; margin-bottom: 15px; }

    .player-row {
      background: var(--panel-bg);
      margin-bottom: 10px;
      padding: 10px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      gap: 10px;
      font-size: 0.8rem;
      border-left: 4px solid transparent;
      cursor: pointer;
    }
    .player-row:hover { background: #303846; }
    .role-badge { width: 20px; height: 20px; border-radius: 50%; display: flex; justify-content: center; align-items: center; font-size: 0.6rem; font-weight: bold; color: white; }
    .r-p { background: var(--orange); } .r-d { background: #27ae60; } .r-c { background: #2980b9; } .r-a { background: #c0392b; }

    /* Area */
    .pitch-wrapper {
      background: var(--orange);
      border-radius: 20px;
      position: relative;
      overflow: hidden;
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 20px;
    }
    .pitch-field {
      width: 100%;
      height: 100%;
      border: 2px solid rgba(255,255,255,0.8);
      border-radius: 10px;
      position: relative;
    }
    /* Linee del campetto */
    .mid-line { position: absolute; top: 50%; width: 100%; height: 2px; background: rgba(255,255,255,0.5); }
    .center-circle { position: absolute; top: 50%; left: 50%; width: 100px; height: 100px; border: 2px solid rgba(255,255,255,0.5); border-radius: 50%; transform: translate(-50%, -50%); }
    .box-top { position: absolute; top: 0; left: 50%; width: 200px; height: 100px; border: 2px solid rgba(255,255,255,0.5); border-top: none; transform: translateX(-50%); }
    .box-bottom { position: absolute; bottom: 0; left: 50%; width: 200px; height: 100px; border: 2px solid rgba(255,255,255,0.5); border-bottom: none; transform: translateX(-50%); }

    /* Giocatori sul campetto */
    .field-player {
      position: absolute;
      display: flex;
      flex-direction: column;
      align-items: center;
      transform: translate(-50%, -50%);
      cursor: pointer;
    }
    .shirt { font-size: 2.5rem; color: #34495e; drop-shadow: 0 2px 4px rgba(0,0,0,0.3); transition: transform 0.2s; }
    .shirt.selected { color: white; transform: scale(1.1); }
    .p-name { background: var(--bg-dark); padding: 2px 8px; border-radius: 4px; font-size: 0.7rem; margin-top: -5px; z-index: 2; font-weight: 700; }

    /* Modulo 4-3-3 (Placeholder) */
    .pos-GK { top: 10%; left: 50%; }
    .pos-D1 { top: 30%; left: 20%; } .pos-D2 { top: 30%; left: 40%; } .pos-D3 { top: 30%; left: 60%; } .pos-D4 { top: 30%; left: 80%; }
    .pos-C1 { top: 55%; left: 30%; } .pos-C2 { top: 55%; left: 50%; } .pos-C3 { top: 55%; left: 70%; }
    .pos-A1 { top: 80%; left: 25%; } .pos-A2 { top: 85%; left: 50%; } .pos-A3 { top: 80%; left: 75%; }

    /* Bottom Actions */
    .pitch-actions {
      position: absolute;
      bottom: 20px;
      width: 90%;
      display: flex;
      justify-content: space-between;
    }
    .action-pill { background: #E3F6FA; color: var(--bg-dark); padding: 8px 15px; border-radius: 20px; font-weight: 700; border: none; cursor: pointer; font-size: 0.8rem; }

    .btn-genera {
      width: 100%;
      background: linear-gradient(90deg, #d35400, #e67e22);
      border: none;
      padding: 15px;
      color: white;
      font-weight: 800;
      border-radius: 15px;
      margin-top: 15px;
      cursor: pointer;
      box-shadow: 0 4px 10px rgba(0,0,0,0.2);
    }

    /* Colonne a destra */
    .detail-card {
      background: var(--panel-bg);
      border-radius: 20px;
      border: 2px solid var(--orange);
      overflow: hidden;
      text-align: center;
    }
    .detail-img { width: 100%; height: 200px; background: #95a5a6; object-fit: cover; }
    .detail-info { padding: 15px; }
    .detail-name { font-size: 1.2rem; font-weight: 800; margin-bottom: 5px; }
    .detail-team { color: var(--text-grey); font-size: 0.9rem; margin-bottom: 15px; }

    .stat-box {
      background: rgba(255,255,255,0.1);
      margin: 5px 0;
      padding: 10px;
      border-radius: 10px;
      display: flex;
      justify-content: space-between;
      font-size: 0.9rem;
    }

    .module-selector {
      margin-top: 20px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      background: var(--panel-bg);
      padding: 10px;
      border-radius: 15px;
    }
    .mod-icon { font-size: 2rem; color: var(--orange); }
    select { background: transparent; color: white; border: none; font-size: 1.2rem; font-weight: 800; outline: none; }
    option { background: var(--panel-bg); }
  </style>
</head>
<body>
<jsp:include page="../includes/navbar.jsp" />

<form action="FormationServlet" method="post" id="formationForm">
  <input type="hidden" name="giornata" value="${currentGiornata != null ? currentGiornata : 1}">

  <div class="layout">
    <!-- Sidebar -->
    <div class="sidebar-list">
      <div class="list-header">ROSA COMPLETA</div>
      <!-- Iterazione sulla Rosa Utente -->
      <c:forEach var="player" items="${mySquad.players}">
        <div class="player-row" draggable="true" data-id="${player.id}">
          <div class="role-badge r-${fn:toLowerCase(player.ruolo)}">${player.ruolo}</div>
          <span>${player.nome}</span>
        </div>
      </c:forEach>
    </div>

    <!-- campo -->
    <div class="pitch-wrapper">
      <div class="pitch-field">
        <div class="mid-line"></div>
        <div class="center-circle"></div>
        <div class="box-top"></div>
        <div class="box-bottom"></div>

        <!-- Giocatori (da rendere dinamici) -->
        <div class="field-player pos-GK"><i class="fas fa-tshirt shirt"></i><div class="p-name">PT</div></div>

        <div class="field-player pos-D1"><i class="fas fa-tshirt shirt"></i><div class="p-name">D</div></div>
        <div class="field-player pos-D2"><i class="fas fa-tshirt shirt"></i><div class="p-name">D</div></div>
        <div class="field-player pos-D3"><i class="fas fa-tshirt shirt"></i><div class="p-name">D</div></div>
        <div class="field-player pos-D4"><i class="fas fa-tshirt shirt"></i><div class="p-name">D</div></div>

        <div class="field-player pos-C1"><i class="fas fa-tshirt shirt"></i><div class="p-name">C</div></div>
        <div class="field-player pos-C2"><i class="fas fa-tshirt shirt selected"></i><div class="p-name">C</div></div>
        <div class="field-player pos-C3"><i class="fas fa-tshirt shirt"></i><div class="p-name">C</div></div>

        <div class="field-player pos-A1"><i class="fas fa-tshirt shirt"></i><div class="p-name">A</div></div>
        <div class="field-player pos-A2"><i class="fas fa-tshirt shirt"></i><div class="p-name">A</div></div>
        <div class="field-player pos-A3"><i class="fas fa-tshirt shirt"></i><div class="p-name">A</div></div>
      </div>

      <div class="pitch-actions">
        <button type="button" class="action-pill">RESET</button>
        <button type="button" class="action-pill" onclick="location.href='calcola-formazione?giornata=${currentGiornata}&modulo=3-4-3'">AUTO</button>
      </div>
    </div>

    <!-- Right Detail -->
    <div class="sidebar-right">
      <div class="detail-card">
        <div class="detail-img">
          <div style="width:100%; height:100%; background:#7f8c8d; display:flex; align-items:center; justify-content:center; color:white; font-size:3rem;"><i class="fas fa-user"></i></div>
        </div>
        <div class="detail-info">
          <div class="detail-name">Seleziona Giocatore.</div>
          <div class="detail-team">---</div>
        </div>
      </div>

      <div class="module-selector">
        <div class="mod-icon"><i class="fas fa-clipboard-list"></i></div>
        <select name="modulo">
          <c:forEach var="mod" items="${modules}">
            <option value="${modules.id}">${modules.id}</option>
          </c:forEach>
        </select>
        <div class="mod-icon"><i class="fas fa-chevron-down"></i></div>
      </div>

      <button type="submit" class="btn-genera">
        <i class="fas fa-check"></i> SCHIERA
      </button>
    </div>
  </div>
</form>

<jsp:include page="../includes/footer.jsp" />
</body>
</html>