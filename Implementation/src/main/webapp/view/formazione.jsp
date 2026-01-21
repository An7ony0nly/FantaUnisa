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
      --col-p: #e67e22; --col-d: #27ae60; --col-c: #2980b9; --col-a: #c0392b;
    }
    * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Montserrat', sans-serif; }

    body {
      background-color: var(--bg-dark);
      color: white;
      height: 100vh;
      display: flex;
      flex-direction: column;
    }

    .layout {
      margin: 40px 30px 0 30px;
      display: grid;
      grid-template-columns: 280px 1fr 300px;
      gap: 20px;
      max-height: 90%;
      height: calc(100vh - 100px);
    }

    /* --- SIDEBAR SX --- */
    .sidebar-wrapper { display: flex; flex-direction: column; height: 100%; gap: 10px; min-height: 0; }
    .list-header { background: #E3F6FA; color: var(--bg-dark); padding: 15px; border-radius: 10px; text-align: center; font-weight: 800; text-transform: uppercase; flex-shrink: 0; }
    .sidebar-list { background: rgba(255,255,255,0.05); border-radius: 15px; padding: 10px; overflow-y: auto; flex-grow: 1; }

    /* FIX DRAG: I figli non catturano il mouse, solo il genitore */
    .player-row {
      background: var(--panel-bg); margin-bottom: 8px; padding: 10px;
      border-radius: 10px; display: flex; align-items: center; gap: 10px;
      font-size: 0.8rem; border-left: 4px solid transparent;
      cursor: grab; transition: background 0.2s;
      user-select: none;
    }
    .player-row * { pointer-events: none; } /* Trucco magico per il Drag */
    .player-row:hover { background: #303846; }
    .player-row:active { cursor: grabbing; }

    .role-badge { width: 22px; height: 22px; border-radius: 50%; display: flex; justify-content: center; align-items: center; font-size: 0.65rem; font-weight: bold; color: white; flex-shrink: 0; }
    .r-p { background: var(--col-p); } .r-d { background: var(--col-d); } .r-c { background: var(--col-c); } .r-a { background: var(--col-a); }

    /* --- CAMPO --- */
    .center-column { display: flex; flex-direction: column; gap: 15px; height: 100%; min-height: 0; }
    .pitch-wrapper {
      background: linear-gradient(135deg, #27ae60, #2ecc71);
      border-radius: 20px; position: relative; border: 4px solid #fff;
      flex-grow: 1; width: 100%;
    }
    .pitch-lines { position: absolute; top: 10px; bottom: 10px; left: 10px; right: 10px; border: 2px solid rgba(255,255,255,0.6); pointer-events: none; }
    .mid-line { position: absolute; top: 50%; width: 100%; height: 2px; background: rgba(255,255,255,0.6); }
    .center-circle { position: absolute; top: 50%; left: 50%; width: 100px; height: 100px; border: 2px solid rgba(255,255,255,0.6); border-radius: 50%; transform: translate(-50%, -50%); }

    /* SLOT GIOCATORE: Z-INDEX ALTO */
    .field-player {
      position: absolute; display: none;
      flex-direction: column; align-items: center;
      transform: translate(-50%, -50%);
      width: 70px; height: 70px; justify-content: center;
      z-index: 100; /* Assicura che sia sopra a tutto */
    }
    .drop-zone { width: 100%; height: 100%; display: flex; flex-direction: column; align-items: center; justify-content: center; cursor: pointer; }

    /* Le icone dentro il campo non devono bloccare il drop */
    .drop-zone * { pointer-events: none; }

    .shirt { font-size: 2.2rem; color: rgba(255,255,255,0.4); filter: drop-shadow(0 2px 3px rgba(0,0,0,0.4)); transition: color 0.3s; }
    .shirt.filled { color: #fff; transform: scale(1.1); }
    .p-name-field { background: rgba(0,0,0,0.7); color: white; padding: 2px 6px; border-radius: 4px; font-size: 0.65rem; margin-top: 2px; font-weight: 700; white-space: nowrap; }

    /* --- PANCHINA --- */
    .bench-wrapper {
      background: rgba(255,255,255,0.1);
      border-radius: 15px; padding: 10px;
      display: flex; justify-content: center; gap: 8px;
      height: 100px; align-items: center; flex-shrink: 0;
    }
    .bench-label { writing-mode: vertical-rl; text-orientation: mixed; font-weight: 800; font-size: 0.7rem; letter-spacing: 2px; color: var(--text-grey); }
    .bench-slot {
      width: 60px; height: 80px;
      border: 2px dashed rgba(255,255,255,0.2);
      border-radius: 8px;
      display: flex; flex-direction: column; align-items: center; justify-content: center;
      position: relative;
    }
    .bench-slot * { pointer-events: none; } /* Anche qui, i figli non bloccano il drop */
    .bench-slot .shirt { font-size: 1.8rem; }

    /* --- SIDEBAR DX --- */
    .sidebar-right { display: flex; flex-direction: column; gap: 20px; }
    .detail-card { background: var(--panel-bg); border-radius: 20px; border: 1px solid rgba(255,255,255,0.1); overflow: hidden; text-align: center; }
    .detail-header { background: var(--orange); padding: 15px; font-weight: 800; text-transform: uppercase; }
    .detail-info { padding: 20px; }
    .detail-name { font-size: 1.2rem; font-weight: 800; margin-bottom: 5px; color: var(--orange); }
    .detail-team { color: var(--text-grey); font-size: 0.8rem; margin-bottom: 20px; font-style: italic; }
    .stat-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid rgba(255,255,255,0.05); font-size: 0.85rem; }

    .module-selector { display: flex; justify-content: space-between; align-items: center; background: white; padding: 12px; border-radius: 10px; margin-bottom: 10px; }
    select { background: transparent; color: var(--bg-dark); border: none; font-size: 1.1rem; font-weight: 800; outline: none; width: 100%; cursor: pointer; }

    .btn-genera { width: 100%; background: linear-gradient(90deg, #d35400, #e67e22); border: none; padding: 15px; color: white; font-size: 1rem; font-weight: 800; border-radius: 15px; cursor: pointer; box-shadow: 0 4px 15px rgba(245, 132, 40, 0.4); text-transform: uppercase; }
    .btn-ai { width: 100%; background: linear-gradient(135deg, #8e44ad, #9b59b6); border: none; padding: 15px; color: white; font-size: 1rem; font-weight: 800; border-radius: 15px; cursor: not-allowed; box-shadow: 0 4px 15px rgba(142, 68, 173, 0.4); text-transform: uppercase; margin-top: 10px; opacity: 0.7; display: flex; justify-content: center; align-items: center; gap: 10px; }
    .btn-pubblica { width: 100%; background: linear-gradient(135deg, #2980b9, #3498db); border: none; padding: 15px; color: white; font-size: 1rem; font-weight: 800; border-radius: 15px; cursor: pointer; box-shadow: 0 4px 15px rgba(41, 128, 185, 0.4); text-transform: uppercase; margin-top: 10px; display: flex; justify-content: center; align-items: center; gap: 10px; transition: transform 0.2s; }
    .btn-pubblica:hover { transform: translateY(-2px); }

    /* MODALE */
    .modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0, 0, 0, 0.7); backdrop-filter: blur(5px); z-index: 1000; display: none; justify-content: center; align-items: center; }
    .modal-content { background: var(--panel-bg); padding: 30px; border-radius: 20px; border: 1px solid rgba(255,255,255,0.1); width: 500px; max-width: 90%; box-shadow: 0 20px 50px rgba(0,0,0,0.5); text-align: center; animation: popupSlide 0.3s ease-out; }
    @keyframes popupSlide { from { transform: translateY(50px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }
    .modal-title { font-size: 1.5rem; font-weight: 800; color: white; margin-bottom: 20px; }
    .modal-textarea { width: 100%; height: 150px; background: rgba(255,255,255,0.05); border: 1px solid rgba(255,255,255,0.1); border-radius: 10px; padding: 15px; color: white; font-family: 'Montserrat', sans-serif; font-size: 1rem; resize: none; margin-bottom: 20px; outline: none; }
    .modal-textarea:focus { border-color: var(--orange); }
    .modal-actions { display: flex; gap: 10px; justify-content: center; }
    .btn-cancel { background: transparent; border: 2px solid #e74c3c; color: #e74c3c; padding: 10px 20px; border-radius: 10px; font-weight: 700; cursor: pointer; }
    .btn-confirm { background: var(--orange); border: none; color: white; padding: 10px 30px; border-radius: 10px; font-weight: 700; cursor: pointer; }
  </style>
</head>
<body>

<jsp:include page="../includes/navbar.jsp" />

<form action="${pageContext.request.contextPath}/FormationServlet" method="post" id="formationForm" onsubmit="return prepareSubmission()">
  <div id="dynamic-inputs"></div>

  <div class="layout">
    <div class="sidebar-wrapper">
      <div class="list-header">ROSA COMPLETA</div>
      <div class="sidebar-list">
        <c:forEach var="player" items="${mySquad.players}">
          <div class="player-row"
               draggable="true"
               ondragstart="drag(event)"
               onclick="showDetailsFromData(this)"
               data-id="${player.id}"
               data-nome="${player.nome}"
               data-squadra="${player.squadra}"
               data-ruolo="${player.ruolo}"
               data-gf="${player.golFatti}"
               data-gs="${player.golSubiti}"
               data-ass="${player.assist}">
            <div class="role-badge r-${fn:toLowerCase(player.ruolo)}">${player.ruolo}</div>
            <span>${player.nome}</span>
          </div>
        </c:forEach>
      </div>
    </div>

    <div class="center-column">
      <div class="pitch-wrapper" id="pitch">
        <div class="pitch-lines"><div class="mid-line"></div><div class="center-circle"></div></div>

        <div class="field-player" id="slot_GK" style="display:flex; top:10%; left:50%;" ondrop="drop(event, this)" ondragover="allowDrop(event)" onclick="showDetailsFromSlot(this)">
          <div class="drop-zone"><i class="fas fa-tshirt shirt"></i></div><div class="p-name-field">PT</div>
        </div>
        <c:forEach begin="1" end="5" var="i">
          <div class="field-player" id="slot_D${i}" ondrop="drop(event, this)" ondragover="allowDrop(event)" onclick="showDetailsFromSlot(this)">
            <div class="drop-zone"><i class="fas fa-tshirt shirt"></i></div><div class="p-name-field">D</div>
          </div>
        </c:forEach>
        <c:forEach begin="1" end="5" var="i">
          <div class="field-player" id="slot_C${i}" ondrop="drop(event, this)" ondragover="allowDrop(event)" onclick="showDetailsFromSlot(this)">
            <div class="drop-zone"><i class="fas fa-tshirt shirt"></i></div><div class="p-name-field">C</div>
          </div>
        </c:forEach>
        <c:forEach begin="1" end="3" var="i">
          <div class="field-player" id="slot_A${i}" ondrop="drop(event, this)" ondragover="allowDrop(event)" onclick="showDetailsFromSlot(this)">
            <div class="drop-zone"><i class="fas fa-tshirt shirt"></i></div><div class="p-name-field">A</div>
          </div>
        </c:forEach>
      </div>

      <div class="bench-wrapper">
        <div class="bench-label">PANCHINA</div>
        <c:forEach begin="1" end="7" var="i">
          <div class="bench-slot" id="bench_${i}" ondrop="drop(event, this)" ondragover="allowDrop(event)" onclick="showDetailsFromSlot(this)">
            <i class="fas fa-tshirt shirt"></i>
            <div class="p-name-field">--</div>
          </div>
        </c:forEach>
      </div>
    </div>

    <div class="sidebar-right">
      <div class="detail-card">
        <div class="detail-header">Statistiche</div>
        <div class="detail-info" id="detailPanel">
          <div class="detail-name" id="det_name">--</div>
          <div class="detail-team" id="det_team">Seleziona un giocatore</div>
          <div class="stat-row"><span>Ruolo</span><span class="stat-val" id="det_role">-</span></div>
          <div class="stat-row"><span>Gol Fatti</span><span class="stat-val" id="det_gf">-</span></div>
          <div class="stat-row"><span>Gol Subiti</span><span class="stat-val" id="det_gs">-</span></div>
          <div class="stat-row"><span>Assist</span><span class="stat-val" id="det_ass">-</span></div>
        </div>
      </div>

      <div class="controls-container">

        <div class="module-selector">
          <select name="giornata" required>
            <option value="" disabled selected>Giornata...</option>
            <c:forEach begin="1" end="38" var="g">
              <option value="${g}" ${g == currentGiornata ? 'selected' : ''}>Giornata ${g}</option>
            </c:forEach>
          </select>
          <i class="fas fa-calendar-alt" style="color:#2c3e50"></i>
        </div>

        <div class="module-selector">
          <select name="modulo" id="modSelect" required onchange="changeModule()">
            <option value="" disabled>Modulo...</option>
            <option value="3-4-3">3-4-3</option>
            <option value="3-5-2">3-5-2</option>
            <option value="4-3-3">4-3-3</option>
            <option value="4-4-2" selected>4-4-2</option>
            <option value="4-5-1">4-5-1</option>
            <option value="5-3-2">5-3-2</option>
            <option value="5-4-1">5-4-1</option>
          </select>
          <i class="fas fa-chevron-down" style="color:#2c3e50"></i>
        </div>

        <button type="submit" class="btn-genera">
          SCHIERA <i class="fas fa-paper-plane" style="margin-left:8px;"></i>
        </button>

        <button type="button" class="btn-ai" title="Funzionalità in arrivo">
          GENERA CON AI <i class="fas fa-robot"></i>
        </button>

        <button type="button" class="btn-pubblica" onclick="openPublishModal()">
          SCHIERA E PUBBLICA <i class="fas fa-share-alt"></i>
        </button>
      </div>
    </div>
  </div>

  <input type="hidden" name="testo" id="hiddenPostText" value="">

  <div class="modal-overlay" id="publishModal">
    <div class="modal-content">
      <div class="modal-title">Pubblica Formazione</div>
      <textarea id="postInput" class="modal-textarea" placeholder="Scrivi un messaggio per la community... (es. 'Questa giornata si punta tutto su Lautaro!')"></textarea>
      <div class="modal-actions">
        <button type="button" class="btn-cancel" onclick="closePublishModal()">ANNULLA</button>
        <button type="button" class="btn-confirm" onclick="confirmPublish()">PUBBLICA</button>
      </div>
    </div>
  </div>

</form>

<script>
  document.addEventListener("DOMContentLoaded", function() {
    changeModule();
  });

  const formations = {
    "3-4-3": { show: ['GK', 'D1', 'D2', 'D3', 'C1', 'C2', 'C3', 'C4', 'A1', 'A2', 'A3'], pos: { D1: {t:'30%', l:'20%'}, D2: {t:'30%', l:'50%'}, D3: {t:'30%', l:'80%'}, C1: {t:'55%', l:'15%'}, C2: {t:'55%', l:'38%'}, C3: {t:'55%', l:'62%'}, C4: {t:'55%', l:'85%'}, A1: {t:'80%', l:'20%'}, A2: {t:'85%', l:'50%'}, A3: {t:'80%', l:'80%'} } },
    "3-5-2": { show: ['GK', 'D1', 'D2', 'D3', 'C1', 'C2', 'C3', 'C4', 'C5', 'A1', 'A2'], pos: { D1: {t:'30%', l:'20%'}, D2: {t:'30%', l:'50%'}, D3: {t:'30%', l:'80%'}, C1: {t:'50%', l:'10%'}, C2: {t:'55%', l:'30%'}, C3: {t:'55%', l:'50%'}, C4: {t:'55%', l:'70%'}, C5: {t:'50%', l:'90%'}, A1: {t:'80%', l:'35%'}, A2: {t:'80%', l:'65%'} } },
    "4-3-3": { show: ['GK', 'D1', 'D2', 'D3', 'D4', 'C1', 'C2', 'C3', 'A1', 'A2', 'A3'], pos: { D1: {t:'30%', l:'15%'}, D2: {t:'30%', l:'38%'}, D3: {t:'30%', l:'62%'}, D4: {t:'30%', l:'85%'}, C1: {t:'55%', l:'25%'}, C2: {t:'55%', l:'50%'}, C3: {t:'55%', l:'75%'}, A1: {t:'80%', l:'20%'}, A2: {t:'85%', l:'50%'}, A3: {t:'80%', l:'80%'} } },
    "4-4-2": { show: ['GK', 'D1', 'D2', 'D3', 'D4', 'C1', 'C2', 'C3', 'C4', 'A1', 'A2'], pos: { D1: {t:'30%', l:'15%'}, D2: {t:'30%', l:'38%'}, D3: {t:'30%', l:'62%'}, D4: {t:'30%', l:'85%'}, C1: {t:'55%', l:'15%'}, C2: {t:'55%', l:'38%'}, C3: {t:'55%', l:'62%'}, C4: {t:'55%', l:'85%'}, A1: {t:'80%', l:'35%'}, A2: {t:'80%', l:'65%'} } },
    "4-5-1": { show: ['GK', 'D1', 'D2', 'D3', 'D4', 'C1', 'C2', 'C3', 'C4', 'C5', 'A1'], pos: { D1: {t:'30%', l:'15%'}, D2: {t:'30%', l:'38%'}, D3: {t:'30%', l:'62%'}, D4: {t:'30%', l:'85%'}, C1: {t:'55%', l:'10%'}, C2: {t:'55%', l:'30%'}, C3: {t:'55%', l:'50%'}, C4: {t:'55%', l:'70%'}, C5: {t:'55%', l:'90%'}, A1: {t:'85%', l:'50%'} } },
    "5-3-2": { show: ['GK', 'D1', 'D2', 'D3', 'D4', 'D5', 'C1', 'C2', 'C3', 'A1', 'A2'], pos: { D1: {t:'30%', l:'10%'}, D2: {t:'30%', l:'30%'}, D3: {t:'30%', l:'50%'}, D4: {t:'30%', l:'70%'}, D5: {t:'30%', l:'90%'}, C1: {t:'55%', l:'25%'}, C2: {t:'55%', l:'50%'}, C3: {t:'55%', l:'75%'}, A1: {t:'80%', l:'35%'}, A2: {t:'80%', l:'65%'} } },
    "5-4-1": { show: ['GK', 'D1', 'D2', 'D3', 'D4', 'D5', 'C1', 'C2', 'C3', 'C4', 'A1'], pos: { D1: {t:'30%', l:'10%'}, D2: {t:'30%', l:'30%'}, D3: {t:'30%', l:'50%'}, D4: {t:'30%', l:'70%'}, D5: {t:'30%', l:'90%'}, C1: {t:'55%', l:'15%'}, C2: {t:'55%', l:'38%'}, C3: {t:'55%', l:'62%'}, C4: {t:'55%', l:'85%'}, A1: {t:'85%', l:'50%'} } }
  };

  function changeModule() {
    const selected = document.getElementById('modSelect').value;
    const config = formations[selected];
    if (!config) return;

    document.querySelectorAll('.field-player').forEach(el => {
      if (el.id !== 'slot_GK') el.style.display = 'none';
    });

    config.show.forEach(slotId => {
      const el = document.getElementById('slot_' + slotId);
      if (el) {
        el.style.display = 'flex';
        if (config.pos[slotId]) {
          el.style.top = config.pos[slotId].t;
          el.style.left = config.pos[slotId].l;
        }
      }
    });
  }

  function allowDrop(ev) { ev.preventDefault(); }

  function drag(ev) {
    // Ora che abbiamo messo pointer-events:none ai figli, ev.target E' SEMPRE .player-row
    const d = ev.target.dataset;
    if (!d || !d.id) return; // Sicurezza

    const pData = { id: d.id, nome: d.nome, squadra: d.squadra, ruolo: d.ruolo, gf: d.gf, gs: d.gs, ass: d.ass };
    ev.dataTransfer.setData("text/plain", JSON.stringify(pData)); // text/plain è più robusto
    ev.dataTransfer.effectAllowed = "copyMove";
  }

  function drop(ev, slotElement) {
    ev.preventDefault();
    const data = ev.dataTransfer.getData("text/plain");
    if(!data) return;

    try {
      const p = JSON.parse(data);

      // --- CONTROLLO DUPLICATI ---
      const allSlots = document.querySelectorAll('.field-player, .bench-slot');
      allSlots.forEach(slot => {
        if (slot.dataset.playerId == p.id) {
          if (slot !== slotElement) {
            clearSlot(slot);
          }
        }
      });

      // Inserisci nel nuovo slot
      const shirt = slotElement.querySelector('.shirt');
      const nameLabel = slotElement.querySelector('.p-name-field');

      if(shirt) shirt.classList.add('filled');
      if(nameLabel) nameLabel.textContent = p.nome;

      slotElement.dataset.playerId = p.id;
      slotElement.dataset.fullData = JSON.stringify(p);
      updateDetailPanel(p);

    } catch (e) {
      console.error("Errore drop:", e);
    }
  }

  function clearSlot(slot) {
    delete slot.dataset.playerId;
    delete slot.dataset.fullData;

    const shirt = slot.querySelector('.shirt');
    if(shirt) shirt.classList.remove('filled');

    const nameLabel = slot.querySelector('.p-name-field');
    const id = slot.id;

    if(nameLabel) {
      if (id.includes('GK')) nameLabel.textContent = 'PT';
      else if (id.includes('slot_D')) nameLabel.textContent = 'D';
      else if (id.includes('slot_C')) nameLabel.textContent = 'C';
      else if (id.includes('slot_A')) nameLabel.textContent = 'A';
      else nameLabel.textContent = '--';
    }
  }

  function showDetailsFromData(el) {
    const d = el.dataset;
    updateDetailPanel({ nome: d.nome, squadra: d.squadra, ruolo: d.ruolo, gf: d.gf, gs: d.gs, ass: d.ass });
  }

  function showDetailsFromSlot(slotElement) {
    if (slotElement.dataset.fullData) {
      updateDetailPanel(JSON.parse(slotElement.dataset.fullData));
    } else {
      document.getElementById('det_name').textContent = "Vuoto";
      document.getElementById('det_team').textContent = "--";
      resetStats();
    }
  }

  function updateDetailPanel(p) {
    document.getElementById('det_name').textContent = p.nome;
    document.getElementById('det_team').textContent = p.squadra;
    document.getElementById('det_role').textContent = p.ruolo;
    document.getElementById('det_gf').textContent = p.gf;
    document.getElementById('det_gs').textContent = p.gs;
    document.getElementById('det_ass').textContent = p.ass;
  }

  function resetStats() {
    document.getElementById('det_role').textContent = "-";
    document.getElementById('det_gf').textContent = "-";
    document.getElementById('det_gs').textContent = "-";
    document.getElementById('det_ass').textContent = "-";
  }

  function prepareSubmission() {
    const formContainer = document.getElementById('dynamic-inputs');
    formContainer.innerHTML = '';
    const activeModule = document.getElementById('modSelect').value;
    if (!activeModule) { alert("Seleziona un modulo!"); return false; }

    const visibleSlots = document.querySelectorAll('.field-player[style*="display: flex"], #slot_GK');
    let countTitolari = 0;

    visibleSlots.forEach(slot => {
      if (slot.dataset.playerId) {
        const pData = JSON.parse(slot.dataset.fullData);
        const input = document.createElement('input');
        input.type = 'hidden';
        input.name = 'giocatori';
        input.value = pData.id + ':' + pData.ruolo + ':titolare';
        formContainer.appendChild(input);
        countTitolari++;
      }
    });

    if (countTitolari !== 11) {
      alert("Devi schierare esattamente 11 titolari! Ne hai messi " + countTitolari);
      return false;
    }

    const benchSlots = document.querySelectorAll('.bench-slot');
    benchSlots.forEach(slot => {
      if (slot.dataset.playerId) {
        const pData = JSON.parse(slot.dataset.fullData);
        const input = document.createElement('input');
        input.type = 'hidden';
        input.name = 'giocatori';
        input.value = pData.id + ':' + pData.ruolo + ':panchina';
        formContainer.appendChild(input);
      }
    });
    return true;
  }

  function openPublishModal() {
    if (!validateFormationRules()) {
      return;
    }
    document.getElementById('publishModal').style.display = 'flex';
  }

  function closePublishModal() {
    document.getElementById('publishModal').style.display = 'none';
    document.getElementById('postInput').value = '';
  }

  function confirmPublish() {
    const text = document.getElementById('postInput').value;
    if (text.trim() === "") {
      alert("Inserisci un testo per il post!");
      return;
    }
    document.getElementById('hiddenPostText').value = text;
    closePublishModal();
    if (prepareSubmission()) {
      document.getElementById('formationForm').submit();
    }
  }

  function validateFormationRules() {
    const activeModule = document.getElementById('modSelect').value;
    if (!activeModule) { alert("Seleziona un modulo!"); return false; }

    const visibleSlots = document.querySelectorAll('.field-player[style*="display: flex"], #slot_GK');
    let countTitolari = 0;
    visibleSlots.forEach(slot => {
      if (slot.dataset.playerId) countTitolari++;
    });

    if (countTitolari !== 11) {
      alert("Devi schierare esattamente 11 titolari prima di pubblicare!");
      return false;
    }
    return true;
  }
</script>

<jsp:include page="../includes/footer.jsp" />
</body>
</html>