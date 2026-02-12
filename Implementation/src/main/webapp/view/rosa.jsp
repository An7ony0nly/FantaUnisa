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
      align-items: flex-end;
      margin-bottom: 30px;
      gap: 15px;
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

    .btn-save {
      background-color: var(--orange);
      color: white;
      border: none;
      padding: 12px 25px;
      border-radius: 15px;
      font-weight: 700;
      cursor: pointer;
      text-transform: uppercase;
      box-shadow: 0 4px 15px rgba(245, 132, 40, 0.3);
      transition: transform 0.2s, background-color 0.2s;
      height: 45px;
      display: flex;
      align-items: center;
    }
    .btn-save:hover {
      background-color: #d35400;
      transform: translateY(-2px);
    }

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
      cursor: pointer;
      position: relative;
    }

    .player-card:hover { transform: translateY(-3px); background: #303846; }

    /* Stile per i giocatori già in rosa */
    .player-card.already-owned {
      border: 1px solid rgba(46, 204, 113, 0.4);
    }

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
      margin-right: 10px;
    }

    .p-check {
      width: 20px;
      height: 20px;
      accent-color: var(--orange);
      cursor: pointer;
    }

    /* Icona check rosa */
    .in-squad-icon {
      color: #2ecc71;
      font-size: 0.8rem;
      margin-left: 5px;
    }

  </style>
</head>
<body>
<jsp:include page="../includes/navbar.jsp" />

<form method="post" action="${pageContext.request.contextPath}/SquadServlet">

  <div class="top-bar">
    <div class="search-container">
      <i class="fas fa-search search-icon"></i>
      <input type="text" id="searchInput" placeholder="Cerca giocatore..." onkeydown="return event.key != 'Enter';">
    </div>
    <button type="submit" class="btn-save">
      <i class="fas fa-save" style="margin-right: 8px;"></i> Salva Rosa
    </button>
  </div>

  <div class="grid-roles">
    <c:set var="roles" value="${fn:split('P,D,C,A', ',')}" />
    <c:set var="roleLabels" value="${fn:split('Portieri,Difensori,Centrocampisti,Attaccanti', ',')}" />
    <c:set var="roleClasses" value="${fn:split('p,d,c,a', ',')}" />

    <c:forEach var="i" begin="0" end="3">
      <div class="role-column col-${roleClasses[i]}">
        <div class="role-header h-${roleClasses[i]}">${roleLabels[i]}</div>

        <c:forEach var="player" items="${allPlayers}">
          <c:if test="${player.ruolo eq roles[i]}">

            <%-- Logica per controllare se il giocatore è già in rosa --%>
            <c:set var="isOwned" value="false" />
            <c:forEach var="owned" items="${mySquad.players}">
              <c:if test="${owned.id == player.id}">
                <c:set var="isOwned" value="true" />
              </c:if>
            </c:forEach>

            <div class="player-card ${isOwned ? 'already-owned' : ''}">
              <div class="p-avatar"><i class="fas fa-user"></i></div>
              <div class="p-info">
                <span class="p-name">
                  <c:out value="${player.nome}"/>
                  <c:if test="${isOwned}">
                    <i class="fas fa-check-circle in-squad-icon" title="Già nella tua rosa"></i>
                  </c:if>
                </span>
                <span class="p-team"><c:out value="${player.squadra}"/></span>
              </div>
              <div class="p-price"><c:out value="${player.fantamedia}"/></div>

                <%-- La checkbox apparirà pre-selezionata se il giocatore è già in rosa --%>
              <input type="checkbox" name="selectedPlayers" value="${player.id}" class="p-check"
                ${isOwned ? 'checked' : ''} />
            </div>
          </c:if>
        </c:forEach>
      </div>
    </c:forEach>
  </div>

</form>

<script>
  document.addEventListener("DOMContentLoaded", function() {
    const searchInput = document.getElementById('searchInput');
    const playerCards = document.querySelectorAll('.player-card');

    searchInput.addEventListener('input', function(e) {
      const searchText = e.target.value.toLowerCase();
      playerCards.forEach(card => {
        const name = card.querySelector('.p-name').textContent.toLowerCase();
        const team = card.querySelector('.p-team').textContent.toLowerCase();
        card.style.display = (name.includes(searchText) || team.includes(searchText)) ? "flex" : "none";
      });
    });

    playerCards.forEach(card => {
      card.addEventListener('click', function(e) {
        if (e.target.tagName === 'INPUT') return;
        const checkbox = this.querySelector('input[type="checkbox"]');
        checkbox.checked = !checkbox.checked;
      });
    });
  });
</script>

<jsp:include page="../includes/footer.jsp" />
</body>
</html>