<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="it">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>FantaUnisa - Accedi o Registrati</title>
  <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;600;700;800;900&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
  <style>
    :root {
      --bg-dark: #1C212E;
      --orange: #F58428;
      --orange-hover: #d35400;
      --card-light: #EAF4F8;
      --text-dark: #2c3e50;
      --text-grey: #bdc3c7;
      --error-red: #e74c3c;
      --success-green: #2ecc71;
    }

    * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Montserrat', sans-serif; }

    body {
      background-color: var(--bg-dark);
      color: white;
      min-height: 100vh;
      display: flex;
      flex-direction: column;
    }

    /* --- NAVBAR --- */
    nav {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20px 5%;
      background: rgba(28, 33, 46, 0.95);
      backdrop-filter: blur(10px);
      border-bottom: 1px solid rgba(255,255,255,0.05);
      position: sticky;
      top: 0;
      z-index: 1000;
    }

    .logo {
      display: flex;
      align-items: center;
      gap: 10px;
      font-weight: 900;
      font-size: 1.4rem;
      color: white;
      text-decoration: none;
    }

    .logo-circle {
      width: 40px;
      height: 40px;
      background: white;
      color: var(--orange);
      border-radius: 50%;
      display: flex;
      justify-content: center;
      align-items: center;
      font-size: 1.2rem;
      box-shadow: 0 0 15px rgba(245, 132, 40, 0.3);
    }

    /* --- MAIN CONTENT --- */
    .main-wrapper {
      flex: 1;
      display: flex;
      justify-content: center;
      align-items: center;
      padding: 60px 20px;
      position: relative;
    }

    /* Background Blobs */
    .blob {
      position: absolute;
      border-radius: 50%;
      filter: blur(90px);
      z-index: 0;
      opacity: 0.15;
      pointer-events: none;
    }
    .b1 { width: 500px; height: 500px; background: var(--orange); top: -100px; left: -100px; }
    .b2 { width: 400px; height: 400px; background: #365B95; bottom: -50px; right: -50px; }

    /* Container delle due card */
    .auth-container {
      display: flex;
      gap: 30px; /* Distanza tra le card */
      max-width: 1000px;
      width: 100%;
      position: relative;
      z-index: 1;
      align-items: stretch;
    }

    .card {
      background: var(--card-light);
      border-radius: 25px;
      padding: 40px;
      color: var(--text-dark);
      box-shadow: 0 15px 40px rgba(0,0,0,0.3);
      display: flex;
      flex-direction: column;
      justify-content: center;
      flex: 1;
      transition: transform 0.3s;
    }

    .card:hover {
      transform: translateY(-5px);
    }

    .card h2 {
      font-size: 1.8rem;
      font-weight: 800;
      margin-bottom: 10px;
      color: var(--bg-dark);
      display: flex;
      align-items: center;
      gap: 10px;
    }

    .card p.subtitle {
      color: #7f8c8d;
      font-size: 0.9rem;
      margin-bottom: 30px;
      line-height: 1.4;
      font-weight: 500;
    }

    /* Form Styles */
    .form-group {
      margin-bottom: 20px;
    }

    .form-group label {
      display: block;
      margin-bottom: 8px;
      font-weight: 700;
      font-size: 0.85rem;
      text-transform: uppercase;
      color: var(--bg-dark);
    }

    .form-group input {
      width: 100%;
      padding: 12px 15px;
      border-radius: 12px;
      border: 2px solid #D1D9E6;
      background: white;
      font-size: 0.95rem;
      outline: none;
      transition: border-color 0.3s, box-shadow 0.3s;
      color: var(--text-dark);
    }

    .form-group input:focus {
      border-color: var(--orange);
      box-shadow: 0 0 0 3px rgba(245, 132, 40, 0.1);
    }

    .row-inputs {
      display: flex;
      gap: 15px;
    }
    .row-inputs .form-group {
      flex: 1;
    }

    .btn {
      width: 100%;
      padding: 15px;
      border-radius: 12px;
      font-weight: 800;
      border: none;
      cursor: pointer;
      font-size: 1rem;
      margin-top: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 10px;
      transition: transform 0.2s;
    }

    .btn:hover { transform: translateY(-2px); }

    .btn-login {
      background: var(--orange);
      color: white;
      box-shadow: 0 5px 15px rgba(245, 132, 40, 0.3);
    }

    .btn-register {
      background: var(--bg-dark);
      color: white;
      box-shadow: 0 5px 15px rgba(28, 33, 46, 0.3);
    }

    .forgot-link {
      display: block;
      text-align: right;
      margin-bottom: 20px;
      font-size: 0.8rem;
      color: #365B95;
      text-decoration: none;
      font-weight: 600;
    }

    .alert-box {
      padding: 10px;
      border-radius: 8px;
      margin-bottom: 15px;
      font-size: 0.9rem;
      font-weight: 600;
    }
    .alert-error { background-color: rgba(231, 76, 60, 0.2); color: var(--error-red); border: 1px solid var(--error-red); }
    .alert-success { background-color: rgba(46, 204, 113, 0.2); color: var(--success-green); border: 1px solid var(--success-green); }

    /* --- FOOTER --- */
    footer {
      padding: 40px 10%;
      background: #11141b;
      border-top: 1px solid rgba(255,255,255,0.05);
      margin-top: auto;
    }

    .footer-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-wrap: wrap;
      gap: 20px;
    }

    .footer-logo {
      font-weight: 900;
      font-size: 1.2rem;
      color: white;
      display: flex;
      align-items: center;
      gap: 10px;
    }

    .footer-links {
      display: flex;
      gap: 20px;
    }

    .footer-links a {
      color: var(--text-grey);
      text-decoration: none;
      font-size: 0.9rem;
      transition: color 0.2s;
    }

    .footer-links a:hover { color: var(--orange); }

    .copyright {
      width: 100%;
      text-align: center;
      margin-top: 30px;
      padding-top: 20px;
      border-top: 1px solid rgba(255,255,255,0.05);
      font-size: 0.8rem;
      color: #555;
    }

    /* Responsive Mobile */
    @media (max-width: 900px) {
      .auth-container { flex-direction: column; max-width: 450px; }
      .nav-logo-text { display: none; }
    }
  </style>
</head>
<body>

<jsp:include page="../includes/navbar.jsp" />

<!-- Main Content -->
<div class="main-wrapper">
  <div class="blob b1"></div>
  <div class="blob b2"></div>

  <div class="auth-container">

    <!-- CARD 1: LOGIN -->
    <div class="card login-card">
      <h2>Accedi</h2>
      <p class="subtitle">Inserisci le tue credenziali</p>

      <%-- Gestione Errori Login --%>
      <c:if test="${not empty param.error or not empty requestScope.error}">
        <div class="alert-box alert-error">
          <c:out value="${param.error}" default="${requestScope.error}"/>
        </div>
      </c:if>
      <c:if test="${not empty param.msg}">
        <div class="alert-box alert-success">
          <c:out value="${param.msg}"/>
        </div>
      </c:if>

      <form action="${pageContext.request.contextPath}/LoginServlet" method="post">
        <div class="form-group">
          <label>Email</label>
          <div style="position:relative">
            <input type="email" name="email" placeholder="m.rossi@studenti.unisa.it" required>
            <i class="fas fa-envelope" style="position:absolute; right:15px; top:13px; color:#bdc3c7;"></i>
          </div>
        </div>

        <div class="form-group">
          <label>Password</label>
          <div style="position:relative">
            <input type="password" name="password" placeholder="••••••••" required>
            <i class="fas fa-lock" style="position:absolute; right:15px; top:13px; color:#bdc3c7;"></i>
          </div>
        </div>

        <a href="#" onclick="promptForgotPassword()" class="forgot-link">Password dimenticata?</a>

        <button type="submit" class="btn btn-login">
          ENTRA <i class="fas fa-sign-in-alt"></i>
        </button>
      </form>
    </div>

    <!-- CARD 2: REGISTRAZIONE -->
    <div class="card register-card">
      <h2>Registrati</h2>
      <p class="subtitle">Nuovo su FantaUnisa? Crea il tuo account.</p>

      <form action="${pageContext.request.contextPath}/RegisterServlet" method="post">
        <div class="row-inputs">
          <div class="form-group">
            <label>Nome</label>
            <input type="text" name="nome" placeholder="Mario" required>
          </div>
          <div class="form-group">
            <label>Cognome</label>
            <input type="text" name="cognome" placeholder="Rossi" required>
          </div>
        </div>

        <div class="form-group">
          <label>Username</label>
          <input type="text" name="username" placeholder="MarioRossi00" required>
        </div>

        <div class="form-group">
          <label>Email</label>
          <input type="email" name="email" placeholder="@studenti.unisa.it" required>
        </div>

        <div class="form-group">
          <label>Password</label>
          <input type="password" name="password" placeholder="Min. 8 caratteri" required minlength="8">
        </div>

        <button type="submit" class="btn btn-register">
          CREA ACCOUNT <i class="fas fa-user-plus"></i>
        </button>
      </form>
    </div>

  </div>
</div>
<script>
  function promptForgotPassword() {
    let email = prompt("Inserisci la tua email per il reset:");
    if(email) {
      let form = document.createElement('form');
      form.method = 'POST';
      form.action = 'ForgotPasswordServlet';
      let input = document.createElement('input');
      input.type = 'hidden';
      input.name = 'email';
      input.value = email;
      form.appendChild(input);
      document.body.appendChild(form);
      form.submit();
    }
  }
</script>
<jsp:include page="../includes/footer.jsp" />
</body>
</html>
