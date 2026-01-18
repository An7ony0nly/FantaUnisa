<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<style>

  footer {
    padding: 60px 10% 20px;
    background: #11141b;
    border-top: 1px solid rgba(255,255,255,0.05);
  }

  .footer-content {
    display: grid;
    grid-template-columns: 2fr 1fr 1fr;
    gap: 50px;
    margin-bottom: 50px;
  }

  .footer-logo {
    font-weight: 900;
    font-size: 1.5rem;
    margin-bottom: 20px;
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .footer-desc {
    color: var(--text-grey);
    line-height: 1.6;
    max-width: 300px;
  }

  .footer-col h4 {
    color: white;
    margin-bottom: 20px;
    font-weight: 700;
  }

  .footer-col ul {
    list-style: none;
  }

  .footer-col li {
    margin-bottom: 12px;
  }

  .footer-col a {
    color: var(--text-grey);
    text-decoration: none;
    transition: color 0.2s;
  }

  .footer-col a:hover {
    color: var(--orange);
  }

  .copyright {
    text-align: center;
    padding-top: 20px;
    border-top: 1px solid rgba(255,255,255,0.05);
    color: #555;
    font-size: 0.8rem;
  }
</style>

<footer>
  <div class="footer-content">
    <div class="footer-brand">
      <div class="footer-logo">
        <div class="logo-circle" style="width:30px; height:30px; font-size:0.9rem; margin-right:10px;">
          <img style="border-radius: 100%; width: 100%" src="${pageContext.request.contextPath}/images/logox.jpeg">
        </div>
        FANTAUNISA
      </div>
      <p class="footer-desc">
        Il tuo tool preferito per il fantacalcio.
      </p>
    </div>

    <div class="footer-col">
      <h4>Link Utili</h4>
      <ul>
        <li><a href="#">Home</a></li>
        <li><a href="#">Regolamento</a></li>
        <li><a href="#">Contatti</a></li>
        <li><a href="#">Privacy Policy</a></li>
      </ul>
    </div>

    <div class="footer-col">
      <h4>Seguici</h4>
      <ul>
        <li><a href="#"><i class="fab fa-instagram"></i> Instagram</a></li>
        <li><a href="#"><i class="fab fa-facebook"></i> Facebook</a></li>
        <li><a href="#"><i class="fab fa-telegram"></i> Canale Telegram</a></li>
      </ul>
    </div>
  </div>

  <div class="copyright">
    &copy; 2026 FantaUnisa. Tutti i diritti riservati.
  </div>
</footer>