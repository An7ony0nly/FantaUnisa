<%@ page import="subsystems.access_profile.model.Role" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Fantaunisa - Aggiorna Dati</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/styles/admin_upload.css">
</head>
<body>
<jsp:include page="../includes/navbar.jsp" />

<%
  Role role = (Role) session.getAttribute("role");
  if (role != Role.GESTORE_DATI)
    request.getRequestDispatcher("login.jsp").forward(request, response);
%>
<div class="container">
  <div class="admin-tabs">
    <a class="admin-tab-btn" href="admin_moderation.jsp">MODERAZIONE</a>
    <a class="admin-tab-btn active" href="admin_upload.jsp">AGGIORNA DATI</a>
  </div>

  <form action="${pageContext.request.contextPath}/StatisticsImportServlet" method="post" enctype='multipart/form-data'>
  <div class="upload-container">
    <h2>CARICA FILE</h2>

    <div class="file-selector">
      <input id="giornata" type="text" name="giornata" placeholder="Giornata">
      <input type="file" name="file" class="file-input" placeholder="Nessun file selezionato" readonly>
    </div>

    <div class="upload-actions">
      <button class="upload-btn" id="uploadBtn" type="button">
        <span class="spinner">⚙️</span>
        UPLOAD (Anteprima)
      </button>
      <button class="cancel-btn">CANCELLA</button>
    </div>

    <div class="progress-container" id="progressContainer" style="display: none;">
      <div class="progress-label">5% UPLOAD</div>
      <div class="progress-bar">
        <div class="progress-fill" id="progressFill"></div>
      </div>
    </div>
  </div>


    <button type="submit" class="save-btn">SALVA MODIFICHE</button>
  </form>

  <div class="preview-container" id="previewContainer" style="display: none;">
    <button class="close-preview" id="closePreview">✕</button>
    <div class="excel-preview">
      <div class="preview-header">
        <span>Last Update: 23/01/26</span>
      </div>
      <div class="excel-title">
        GESTIONE AITA FANTACALCIO - FOGLIO EXCEL REALIZZATO DA FILIPH.IT
      </div>
      <table class="excel-table">
        <thead>
        <tr>
          <th></th>
          <th style="background: #90EE90;">SQUADRA1</th>
          <th style="background: #FFD700;">SQUADRA2</th>
          <th style="background: #87CEEB;">SQUADRA3</th>
          <th style="background: #FFB6C1;">SQUADRA4</th>
          <th style="background: #FFA500;">SQUADRA5</th>
          <th style="background: #DDA0DD;">SQUADRA6</th>
          <th style="background: #F0E68C;">SQUADRA7</th>
          <th style="background: #98FB98;">SQUADRA8</th>
          <th>TOT</th>
        </tr>
        <tr>
          <th></th>
          <th style="background: #90EE90;">200</th>
          <th style="background: #FFD700;">195</th>
          <th style="background: #87CEEB;">200</th>
          <th style="background: #FFB6C1;">200</th>
          <th style="background: #FFA500;">200</th>
          <th style="background: #DDA0DD;">200</th>
          <th style="background: #F0E68C;">200</th>
          <th style="background: #98FB98;">200</th>
          <th>2131</th>
        </tr>
        </thead>
        <tbody>
        <tr>
          <td>1</td>
          <td>P.M</td>
          <td></td>
          <td></td>
          <td></td>
          <td></td>
          <td></td>
          <td></td>
          <td></td>
          <td>Cariani var</td>
        </tr>
        <tr>
          <td>2</td>
          <td>P</td>
          <td></td>
          <td></td>
          <td></td>
          <td></td>
          <td></td>
          <td></td>
          <td></td>
          <td>Cariani</td>
        </tr>
        <tr>
          <td>3</td>
          <td>P</td>
          <td>28</td>
          <td>28</td>
          <td>28</td>
          <td>28</td>
          <td>28</td>
          <td>28</td>
          <td>28</td>
          <td>216</td>
        </tr>
        <tr>
          <td>4</td>
          <td>D</td>
          <td></td>
          <td></td>
          <td></td>
          <td></td>
          <td></td>
          <td></td>
          <td></td>
          <td>Calcolati</td>
        </tr>
        <tr>
          <td>5</td>
          <td>D/M</td>
          <td>28</td>
          <td>27</td>
          <td>28</td>
          <td>28</td>
          <td>28</td>
          <td>28</td>
          <td>28</td>
          <td>218</td>
        </tr>
        <tr>
          <td>6</td>
          <td>P/A</td>
          <td>N.J</td>
          <td>N.J</td>
          <td>N.J</td>
          <td>N.J</td>
          <td>N.J</td>
          <td>N.J</td>
          <td>N.J</td>
          <td>216</td>
        </tr>
        </tbody>
      </table>
      <div class="excel-section">
        <div class="section-title" style="background: #FFFF00;">ROSE</div>
        <table class="rose-table">
          <tr>
            <td>Por</td>
            <td></td>
            <td></td>
            <td></td>
            <td></td>
            <td></td>
            <td></td>
            <td></td>
          </tr>
          <tr>
            <td>Dif</td>
            <td></td>
            <td></td>
            <td></td>
            <td></td>
            <td></td>
            <td></td>
            <td></td>
          </tr>
          <tr>
            <td>P.A</td>
            <td></td>
            <td></td>
            <td></td>
            <td></td>
            <td></td>
            <td></td>
            <td></td>
          </tr>
          <tr>
            <td>Ctr</td>
            <td></td>
            <td></td>
            <td></td>
            <td></td>
            <td></td>
            <td></td>
            <td></td>
          </tr>
        </table>
        <div class="legend">
          <div>Costo medio POR</div>
          <div>1,00</div>
          <div>Costo medio DIF</div>
          <div>1,61</div>
        </div>
      </div>
    </div>
  </div>
</div>

<jsp:include page="../includes/footer.jsp" />

<script src="${pageContext.request.contextPath}/scripts/admin_upload.js"></script>
</body>
</html>