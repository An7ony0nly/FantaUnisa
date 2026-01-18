<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Test Connessione</title>
    <style>
        body { font-family: sans-serif; padding: 20px; background-color: #f0f8ff; }
        .box { border: 2px solid #007bff; padding: 20px; background: white; border-radius: 8px; }
        h1 { color: #007bff; }
        li { margin-bottom: 5px; }
        .important { color: red; font-weight: bold; }
    </style>
</head>
<body>
<div class="box">
    <h1>✅ TEST RIUSCITO!</h1>
    <p>Se vedi questa pagina, Tomcat sta leggendo correttamente la cartella <code>/view</code>.</p>
    <hr>
    <h3>Dati Utili per il Debug:</h3>
    <ul>
        <li><b>Data Server:</b> <%= new java.util.Date() %></li>
        <li><b>Il tuo URL Base (Context Path):</b> <span class="important"><%= request.getContextPath() %></span></li>
        <li><b>Indirizzo completo richiesto:</b> <%= request.getRequestURL() %></li>
    </ul>
    <p><i>Usa il "Context Path" qui sopra come prefisso per tutti i tuoi link CSS/JS!</i></p>
</div>
</body>
</html>