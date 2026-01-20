<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Fantaunisa - Community</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/styles/community.css">
</head>
<body>
<jsp:include page="../includes/navbar.jsp" />

<div class="container">
  <!-- Post 1 -->
  <div class="post-container">
    <div class="post-left">
      <div class="formation-card">
        <div class="field">
          <!-- Goalkeeper -->
          <div class="player-row">
            <div class="player goalkeeper">
              <div class="jersey"></div>
              <span class="player-name">Nome</span>
            </div>
          </div>

          <!-- Defenders -->
          <div class="player-row">
            <div class="player">
              <div class="jersey"></div>
              <span class="player-name">Barone</span>
            </div>
            <div class="player">
              <div class="jersey"></div>
              <span class="player-name">Nome</span>
            </div>
            <div class="player">
              <div class="jersey"></div>
              <span class="player-name">Barone</span>
            </div>
          </div>

          <!-- Midfielders -->
          <div class="player-row">
            <div class="player">
              <div class="jersey"></div>
              <span class="player-name">Nome</span>
            </div>
            <div class="player">
              <div class="jersey"></div>
              <span class="player-name">Nome</span>
            </div>
            <div class="player">
              <div class="jersey"></div>
              <span class="player-name">Nome</span>
            </div>
            <div class="player">
              <div class="jersey"></div>
              <span class="player-name">Nome</span>
            </div>
          </div>

          <!-- Forwards -->
          <div class="player-row">
            <div class="player">
              <div class="jersey"></div>
              <span class="player-name">Barone</span>
            </div>
            <div class="player">
              <div class="jersey"></div>
              <span class="player-name">Nome</span>
            </div>
            <div class="player">
              <div class="jersey"></div>
              <span class="player-name">Barone</span>
            </div>
          </div>
        </div>
      </div>
      <div class="post-actions">
        <button class="action-btn">❤️</button>
        <button class="action-btn">💬</button>
      </div>
    </div>

    <div class="post-right">
      <div class="post-header">
        <span class="user-icon-small">👤</span>
        <div class="post-info">
          <h3>Giovanni_5647</h3>
          <p class="post-text">Dubbi su Toloi... Consigli?</p>
        </div>
      </div>

      <div class="comments">
        <div class="comment">
          <span class="user-icon-small">👤</span>
          <div class="comment-content">
            <div class="comment-header">
              <span class="comment-user">Giuseppe_8448</span>
              <div class="comment-actions">
                <button class="action-icon">❤️</button>
                <button class="action-icon">💬</button>
                <button class="action-icon">ℹ️</button>
              </div>
            </div>
            <p class="comment-text">Ti suggerisco Bastoni</p>
          </div>
        </div>

        <div class="comment reply">
          <span class="user-icon-small">👤</span>
          <div class="comment-content">
            <div class="comment-header">
              <span class="comment-user">Francesco_4921</span>
              <div class="comment-actions">
                <button class="action-icon">❤️</button>
                <button class="action-icon">💬</button>
                <button class="action-icon">ℹ️</button>
              </div>
            </div>
            <p class="comment-text">Forse non è una buona idea</p>
          </div>
        </div>

        <div class="comment">
          <span class="user-icon-small">👤</span>
          <div class="comment-content">
            <div class="comment-header">
              <span class="comment-user">Alessio_0642</span>
              <div class="comment-actions">
                <button class="action-icon">❤️</button>
                <button class="action-icon">💬</button>
                <button class="action-icon">ℹ️</button>
              </div>
            </div>
            <p class="comment-text">Sostituisci Pinemonti</p>
          </div>
        </div>
      </div>

      <div class="comment-input">
        <input type="text" placeholder="Scrivi un commento...">
        <button class="send-btn">✈️</button>
      </div>
    </div>
  </div>

  <hr class="divider">

  <!-- Post 2 -->
  <div class="post-container">
    <div class="post-left">
      <div class="formation-card">
        <div class="field">
          <!-- Goalkeeper -->
          <div class="player-row">
            <div class="player goalkeeper">
              <div class="jersey"></div>
              <span class="player-name">Nome</span>
            </div>
          </div>

          <!-- Defenders -->
          <div class="player-row">
            <div class="player">
              <div class="jersey"></div>
              <span class="player-name">Nome</span>
            </div>
            <div class="player">
              <div class="jersey"></div>
              <span class="player-name">Nome</span>
            </div>
          </div>
        </div>
      </div>
      <div class="post-actions">
        <button class="action-btn">❤️</button>
        <button class="action-btn">💬</button>
      </div>
    </div>

    <div class="post-right">
      <div class="post-header">
        <span class="user-icon-small">👤</span>
        <div class="post-info">
          <h3>Alberto_8063</h3>
          <p class="post-text">Giornata conclusa</p>
        </div>
      </div>
    </div>
  </div>
</div>

<jsp:include page="../includes/footer.jsp"/>
</body>
</html>

<!---->