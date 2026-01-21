<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="it">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>FantaUnisa - Community</title>

  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
  <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;500;600;700;800&display=swap" rel="stylesheet">

  <link rel="stylesheet" href="${pageContext.request.contextPath}/styles/community.css">
</head>
<body>

<jsp:include page="../includes/navbar.jsp" />

<div class="container">

  <c:if test="${not empty param.error}">
    <div style="background: #e74c3c; color: white; padding: 15px; border-radius: 10px; margin-bottom: 20px; text-align: center; font-weight: 700; box-shadow: 0 4px 10px rgba(0,0,0,0.2);">
      <i class="fas fa-exclamation-triangle"></i>
      <c:choose>
        <c:when test="${param.error == 'EmptyContent'}">Il post non può essere vuoto!</c:when>
        <c:when test="${param.error == 'EmptyComment'}">Scrivi un commento prima di inviare.</c:when>
        <c:when test="${param.error == 'ReportFailed'}">Impossibile inviare la segnalazione.</c:when>
        <c:otherwise>Si è verificato un errore generico.</c:otherwise>
      </c:choose>
    </div>
  </c:if>

  <c:if test="${param.msg == 'ReportSent'}">
    <div style="background: #27ae60; color: white; padding: 15px; border-radius: 10px; margin-bottom: 20px; text-align: center; font-weight: 700;">
      <i class="fas fa-check-circle"></i> Segnalazione inviata con successo. Grazie!
    </div>
  </c:if>

  <c:forEach var="post" items="${posts}">

    <div class="post-card" id="post-${post.id}">

      <c:if test="${post.formation != null}">
        <div class="post-visual">
          <div class="mini-pitch">
            <div class="pitch-mid-line"></div>
            <div class="pitch-circle"></div>
            <div class="pitch-box-top"></div>
            <div class="pitch-box-bot"></div>

            <div class="pitch-row">
              <c:forEach var="p" items="${post.formation.players}">
                <c:if test="${p.ruolo == 'P'}">
                  <div class="pitch-player">
                    <i class="fas fa-tshirt p-shirt role-P"></i>
                    <span class="p-label"><c:out value="${p.nome}"/></span>
                  </div>
                </c:if>
              </c:forEach>
            </div>

            <div class="pitch-row">
              <c:forEach var="p" items="${post.formation.players}">
                <c:if test="${p.ruolo == 'D'}">
                  <div class="pitch-player">
                    <i class="fas fa-tshirt p-shirt role-D"></i>
                    <span class="p-label"><c:out value="${p.nome}"/></span>
                  </div>
                </c:if>
              </c:forEach>
            </div>

            <div class="pitch-row">
              <c:forEach var="p" items="${post.formation.players}">
                <c:if test="${p.ruolo == 'C'}">
                  <div class="pitch-player">
                    <i class="fas fa-tshirt p-shirt role-C"></i>
                    <span class="p-label"><c:out value="${p.nome}"/></span>
                  </div>
                </c:if>
              </c:forEach>
            </div>

            <div class="pitch-row">
              <c:forEach var="p" items="${post.formation.players}">
                <c:if test="${p.ruolo == 'A'}">
                  <div class="pitch-player">
                    <i class="fas fa-tshirt p-shirt role-A"></i>
                    <span class="p-label"><c:out value="${p.nome}"/></span>
                  </div>
                </c:if>
              </c:forEach>
            </div>

          </div>
        </div>
      </c:if>

      <div class="post-content ${post.formation == null ? 'full-width' : ''}">

        <div class="post-header">
          <div class="user-avatar">
            <i class="fas fa-user"></i>
          </div>
          <div class="user-info">
            <span class="u-name">${fn:substringBefore(post.userEmail, '@')}</span>
            <span class="u-date">
                            <i class="far fa-clock"></i> <c:out value="${post.dataOra}"/>
                        </span>
          </div>
        </div>

        <div class="post-text">
          <c:out value="${post.testo}" />
        </div>

        <div class="post-actions">

          <form action="${pageContext.request.contextPath}/ReactionServlet" method="post" style="display:inline;">
            <input type="hidden" name="postId" value="${post.id}">
            <input type="hidden" name="tipo" value="LIKE">

            <button type="submit" class="action-btn ${post.currentUserReaction == 'LIKE' ? 'active' : ''}">
              <i class="${post.currentUserReaction == 'LIKE' ? 'fas' : 'far'} fa-heart"></i>
              <span>${post.reactionCounts['LIKE'] != null ? post.reactionCounts['LIKE'] : 0}</span>
            </button>
          </form>

          <span style="color: #bdc3c7; font-size: 0.9rem; margin-left: 15px; display: flex; align-items: center; gap: 5px;">
                        <i class="far fa-comment-dots"></i>
                        ${fn:length(post.comments)}
                    </span>

          <button type="button" class="report-btn" onclick="openReportModal(${post.id})" title="Segnala questo post">
            <i class="fas fa-flag"></i>
          </button>
        </div>

        <div class="comments-section">
          <c:forEach var="comment" items="${post.comments}">
            <div class="comment-row">
              <div class="comment-connector"></div>
              <div class="c-avatar">
                <i class="fas fa-user" style="font-size: 0.7rem;"></i>
              </div>
              <div class="c-body">
                <div class="c-user">${fn:substringBefore(comment.userEmail, '@')}</div>
                <div class="c-text"><c:out value="${comment.testo}"/></div>
              </div>
            </div>
          </c:forEach>

          <c:if test="${empty post.comments}">
            <div style="text-align: center; color: #7f8c8d; font-style: italic; font-size: 0.8rem; margin-top: 20px;">
              Nessun commento. Rompi il ghiaccio!
            </div>
          </c:if>
        </div>

        <form action="${pageContext.request.contextPath}/CommentServlet" method="post" class="comment-form">
          <input type="hidden" name="postId" value="${post.id}">
          <input type="text" name="testo" class="c-input" placeholder="Scrivi un commento..." required autocomplete="off">
          <button type="submit" class="c-submit">
            <i class="fas fa-paper-plane"></i>
          </button>
        </form>

      </div>
    </div>

  </c:forEach>

  <c:if test="${empty posts}">
    <div style="text-align: center; padding: 100px 20px;">
      <i class="fas fa-ghost" style="font-size: 4rem; color: #34495e; margin-bottom: 20px;"></i>
      <h2 style="color: #ecf0f1;">È tutto molto silenzioso qui...</h2>
      <p style="color: #95a5a6;">Vai su "Formazione" e condividi la tua rosa con la community!</p>
      <a href="${pageContext.request.contextPath}/FormationServlet"
         style="display:inline-block; margin-top:20px; background:#F58428; color:white; padding:10px 25px; border-radius:30px; text-decoration:none; font-weight:bold;">
        Crea Formazione
      </a>
    </div>
  </c:if>

</div>

<div class="modal-overlay" id="reportModal">
  <div class="modal-content">
    <div class="modal-title">Segnala Post</div>
    <p style="color: #bdc3c7; font-size: 0.9rem; margin-bottom: 15px;">
      Aiutaci a mantenere la community pulita. Perché vuoi segnalare questo contenuto?
    </p>

    <form action="${pageContext.request.contextPath}/ReportServlet" method="post">
      <input type="hidden" name="postId" id="reportPostId" value="">

      <textarea name="motivo" class="modal-textarea" placeholder="Descrivi il problema (es. linguaggio offensivo, spam, contenuti inappropriati...)" required></textarea>

      <div class="modal-actions">
        <button type="button" class="btn-cancel" onclick="closeReportModal()">ANNULLA</button>
        <button type="submit" class="btn-confirm">INVIA SEGNALAZIONE</button>
      </div>
    </form>
  </div>
</div>

<jsp:include page="../includes/footer.jsp"/>

<script>
  function openReportModal(idPost) {
    // 1. Imposta l'ID del post nel form nascosto
    document.getElementById('reportPostId').value = idPost;
    // 2. Mostra il modale
    document.getElementById('reportModal').style.display = 'flex';
  }

  function closeReportModal() {
    document.getElementById('reportModal').style.display = 'none';
    // Pulisci la textarea
    document.querySelector('.modal-textarea').value = '';
  }

  // Chiude il modale se si clicca fuori dalla finestra
  window.onclick = function(event) {
    const modal = document.getElementById('reportModal');
    if (event.target === modal) {
      closeReportModal();
    }
  }
</script>

</body>
</html>