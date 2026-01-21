<%@ page import="subsystems.access_profile.model.Role" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="it">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>FantaUnisa - Aggiorna Dati</title>

  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
  <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;600;700;800&display=swap" rel="stylesheet">

  <link rel="stylesheet" href="${pageContext.request.contextPath}/styles/admin_upload.css">
</head>
<body>

<%
  Role role = (Role) session.getAttribute("role");
  // Controllo semplice del ruolo (come richiesto)
  if (role != Role.GESTORE_DATI) {
    // Nota: in una app reale meglio usare sendRedirect per evitare loop
    if (role != null) {
      response.sendRedirect("index.jsp");
    } else {
      request.getRequestDispatcher("login.jsp").forward(request, response);
    }
    return;
  }
%>

<jsp:include page="../includes/navbar.jsp" />

<div class="container">

  <div class="admin-tabs">
    <a class="admin-tab-btn" href="${pageContext.request.contextPath}/ReportServlet">
      <i class="fas fa-shield-alt"></i> MODERAZIONE
    </a>
    <a class="admin-tab-btn active" href="admin_upload.jsp">
      <i class="fas fa-database"></i> AGGIORNA DATI
    </a>
  </div>

  <form action="${pageContext.request.contextPath}/StatisticsImportServlet" method="post" enctype='multipart/form-data'>

    <div class="upload-container">
      <h2><i class="fas fa-cloud-upload-alt"></i> Carica File Excel</h2>

      <div class="file-selector">
        <input id="giornata" type="text" name="giornata" placeholder="Inserisci numero Giornata (es. 12)" required>
        <input type="file" name="file" class="file-input" accept=".csv, .txt" required>
      </div>

      <div class="upload-actions">
        <button class="btn upload-btn" id="uploadBtn" type="button">
          <span class="spinner" style="display:none"><i class="fas fa-circle-notch"></i></span>
          <i class="fas fa-eye"></i> ANTEPRIMA
        </button>
        <button class="btn cancel-btn" type="reset">
          <i class="fas fa-times"></i> CANCELLA
        </button>
      </div>

      <div class="progress-container" id="progressContainer" style="display: none;">
        <div class="progress-label">0% ELABORAZIONE...</div>
        <div class="progress-bar">
          <div class="progress-fill" id="progressFill"></div>
        </div>
      </div>
    </div>

    <button type="submit" class="save-btn">
      <i class="fas fa-save"></i> SALVA MODIFICHE NEL DB
    </button>

  </form>

  <div class="preview-container" id="previewContainer" style="display: none;">
    <button class="close-preview" id="closePreview"><i class="fas fa-times"></i></button>
    <div class="excel-preview"></div>
  </div>

</div>

<jsp:include page="../includes/footer.jsp" />

<script src="${pageContext.request.contextPath}/scripts/admin_upload.js"></script>
</body>
</html>