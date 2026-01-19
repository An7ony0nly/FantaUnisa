<%@ page import="subsystems.access_profile.model.Role" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Fantaunisa - Moderazione</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/styles/admin_moderation.css">
</head>
<body>
<jsp:include page="../includes/navbar.jsp" />

<%
  Role role = (Role) session.getAttribute("role");
  if (role != Role.GESTORE_UTENTI)
    request.getRequestDispatcher("login.jsp").forward(request, response);
%>
<div class="container">
  <div class="admin-tabs">
    <a class="admin-tab-btn active" href="admin_moderation.jsp">MODERAZIONE</a>
    <a class="admin-tab-btn" href="admin_upload.jsp">AGGIORNA DATI</a>
  </div>

  <div class="reports-container">
    <c:forEach items="${reports}" var="Report">
      <div class="report-card warning">
        <div class="report-header">
          <div class="report-text">
            <strong>REPORT: [SEGNALAZIONE UTENTE]:</strong> ${Report.userEmail} ha segnalato il post ${Report.postId} - ${Report.dataOra}
          </div>
          <button class="alert-icon">⚠️</button>
        </div>
        <div class="report-actions">
          <button class="action-btn expand-btn">
            <span>${Report.motivo}</span>
            <span class="dots">⋮</span>
          </button>
        </div>
    </c:forEach>
  </div>
</div>

<jsp:include page="../includes/footer.jsp" />

</body>
</html>
