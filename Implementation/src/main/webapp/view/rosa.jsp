<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="it">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>FantaUnisa - Crea Rosa</title>
  <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;600;700;800&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
  <style>
    :root {
      --bg-dark: #1C212E;
      --orange: #F58428;
      --col-p: #e67e22;
      --col-d: #27ae60;
      --col-c: #2980b9;
      --col-a: #c0392b;
    }
    * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Montserrat', sans-serif; }

    body {
      background-color: var(--bg-dark);
      color: white;
      min-height: 100vh;
    }

    .top-bar {
      display: flex;
      justify-content: center;
      margin-bottom: 30px;
      position: relative;
    }
    .search-container {
      background: #E3F6FA;
      border-radius: 15px;
      padding: 10px 20px;
      display: flex;
      align-items: center;
      width: 400px;
      margin-top: 40px;
    }
    .search-container input {
      border: none;
      background: transparent;
      outline: none;
      width: 100%;
      margin-left: 10px;
      font-weight: 600;
      color: #2c3e50;
    }
    .search-icon { color: #7f8c8d; }

    .grid-roles {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 20px;
      max-width: 1400px;
      margin: 0 auto 150px auto;
    }

    .role-column {
      background: rgba(255,255,255,0.05);
      border-radius: 20px;
      padding: 15px;
      border-top: 5px solid transparent;
    }

    /* Bordi superiori colorati */
    .col-p { border-color: var(--col-p); }
    .col-d { border-color: var(--col-d); }
    .col-c { border-color: var(--col-c); }
    .col-a { border-color: var(--col-a); }

    .role-header {
      text-align: center;
      padding: 10px;
      background: white;
      color: var(--bg-dark);
      border-radius: 10px;
      margin-bottom: 20px;
      font-weight: 800;
      text-transform: uppercase;
    }
    .h-p { color: var(--col-p); }
    .h-d { color: var(--col-d); }
    .h-c { color: var(--col-c); }
    .h-a { color: var(--col-a); }

    .player-card {
      background: #252B36;
      margin-bottom: 10px;
      padding: 15px;
      border-radius: 15px;
      display: flex;
      align-items: center;
      gap: 15px;
      transition: transform 0.2s;
    }
    .player-card:hover { transform: translateY(-3px); background: #303846; }

    .p-avatar {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      background: #bdc3c7;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #2c3e50;
    }

    .p-info { flex: 1; }
    .p-name { font-weight: 700; font-size: 0.9rem; display: block; }
    .p-team { font-size: 0.7rem; color: #95a5a6; text-transform: uppercase; font-weight: 600; }

    .p-price {
      background: rgba(255,255,255,0.1);
      padding: 5px 10px;
      border-radius: 8px;
      font-size: 0.8rem;
      font-weight: bold;
    }

    .empty-msg {
      color: #7f8c8d;
      font-style: italic;
      text-align: center;
      font-size: 0.8rem;
    }

  </style>
</head>
<body>
<jsp:include page="../includes/navbar.jsp" />
<div class="top-bar">
  <div class="search-container">
    <i class="fas fa-search search-icon"></i>
    <input type="text" placeholder="Cerca giocatore...">
  </div>
</div>

<div class="grid-roles">
  <!-- PORTIERI -->
  <div class="role-column col-p">
    <div class="role-header h-p">Portieri</div>
    <c:forEach var="player" items="${mySquad.players}">
      <c:if test="${player.ruolo eq 'P'}">
        <div class="player-card">
          <div class="p-avatar"><i class="fas fa-user"></i></div>
          <div class="p-info">
            <span class="p-name"><c:out value="${player.nome}"/></span>
            <span class="p-team"><c:out value="${player.squadra}"/></span>
          </div>
          <div class="p-price"><c:out value="${player.fantamedia}"/></div>
        </div>
      </c:if>
    </c:forEach>
  </div>

  <!-- DIFENSORI -->
  <div class="role-column col-d">
    <div class="role-header h-d">Difensori</div>
    <c:forEach var="player" items="${mySquad.players}">
      <c:if test="${player.ruolo eq 'D'}">
        <div class="player-card">
          <div class="p-avatar"><i class="fas fa-user"></i></div>
          <div class="p-info">
            <span class="p-name"><c:out value="${player.nome}"/></span>
            <span class="p-team"><c:out value="${player.squadra}"/></span>
          </div>
          <div class="p-price"><c:out value="${player.fantamedia}"/></div>
        </div>
      </c:if>
    </c:forEach>
  </div>

  <!-- CENTROCAMPISTI -->
  <div class="role-column col-c">
    <div class="role-header h-c">Centrocampisti</div>
    <c:forEach var="player" items="${mySquad.players}">
      <c:if test="${player.ruolo eq 'C'}">
        <div class="player-card">
          <div class="p-avatar"><i class="fas fa-user"></i></div>
          <div class="p-info">
            <span class="p-name"><c:out value="${player.nome}"/></span>
            <span class="p-team"><c:out value="${player.squadra}"/></span>
          </div>
          <div class="p-price"><c:out value="${player.fantamedia}"/></div>
        </div>
      </c:if>
    </c:forEach>
  </div>

  <!-- ATTACCANTI -->
  <div class="role-column col-a">
    <div class="role-header h-a">Attaccanti</div>
    <c:forEach var="player" items="${mySquad.players}">
      <c:if test="${player.ruolo eq 'A'}">
        <div class="player-card">
          <div class="p-avatar"><i class="fas fa-user"></i></div>
          <div class="p-info">
            <span class="p-name"><c:out value="${player.nome}"/></span>
            <span class="p-team"><c:out value="${player.squadra}"/></span>
          </div>
          <div class="p-price"><c:out value="${player.fantamedia}"/></div>
        </div>
      </c:if>
    </c:forEach>
  </div>
</div>
<jsp:include page="../includes/footer.jsp" />
</body>
</html>

<!---->