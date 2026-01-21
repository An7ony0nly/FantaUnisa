<%@ page import="subsystems.access_profile.model.User" %>
<%@ page import="subsystems.access_profile.model.Role" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<style>
    .header-hero {
        position: relative;
        width: 100%;
        height: 380px;
        overflow: hidden;
    }


    .header-hero::before {
        content: "";
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background-image: url('${pageContext.request.contextPath}/images/messi.jpeg');
        background-size: cover;
        background-position: center;
        z-index: -1;
        filter: blur(.9px) brightness(0.7);
        transform: scale(1.05);
    }

    .header-hero::after {
        content: "";
        position: absolute;
        bottom: 0;
        left: 0;
        width: 100%;
        height: 50%;
        background: linear-gradient(to top, rgba(28, 33, 46, .7), transparent);
        z-index: 0;
    }

    .auth-btn-top {
        position: absolute;
        top: 20px;
        right: 20px;
        background-color: #F58428;
        color: white;
        text-decoration: none;
        padding: 10px 20px;
        border-radius: 8px;
        font-weight: 700;
        font-family: 'Montserrat', sans-serif;
        text-transform: uppercase;
        font-size: 0.85rem;
        box-shadow: 0 4px 15px rgba(0,0,0,0.5);
        transition: transform 0.2s, background-color 0.2s;
        z-index: 10;
        display: flex;
        align-items: center;
        gap: 8px;
    }

    .auth-btn-top:hover {
        background-color: #d35400;
        transform: translateY(-2px);
    }

    .navbar-wrapper {
        width: 80%;
        background: transparent;
        display: flex;
        justify-content: center;
        margin: 0 auto
    }

    .navbar-container {
        position: relative;
        width: 100%;
        height: 140px;
        display: flex;
        justify-content: center;
        align-items: flex-start;
        padding: 0 10px;
    }


    .navbar-bg {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100px;
        background: rgba(30, 41, 59, 0.95);
        backdrop-filter: blur(15px);
        border-radius: 0 0 30px 30px;
        box-shadow: 0 10px 40px rgba(0,0,0,0.6);
        border: 1px solid rgba(255,255,255,0.05);
        border-top: none;
        z-index: 1;
    }


    .navbar-grid {
        position: relative;
        z-index: 2;
        width: 100%;
        height: 100%;
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        padding: 20px 20px 0 20px;
    }


    .level-low {
        margin-top: 0;
    }

    .level-high {
        margin-top: 0px;
    }

    .level-top {
        margin-top: -60px;
    }

    /* STILE LOGO */
    .logo-btn {
        width: 90px;
        height: 90px;
        background: linear-gradient(135deg, #ffffff, #e2e8f0);
        border-radius: 50%;
        display: flex;
        justify-content: center;
        align-items: center;
        box-shadow: 0 8px 25px rgba(0,0,0,0.4);
        border: 5px solid #0f172a;
        font-size: 40px;
        text-decoration: none;
        transition: transform 0.2s;
        color: #0f172a;
    }
    .palla{
        width: 90%;
    }

    .top-logo {
        position: absolute;
        top: 20px;
        left: 20px;
        display: flex;
        align-items: center;
        gap: 12px;
        text-decoration: none;
        z-index: 10;
        transition: transform 0.2s;
    }

    .top-logo:hover {
        transform: scale(1.05);
    }

    .logo-circle-small {
        width: 40px;
        height: 40px;
        background: white;
        color: #F58428;
        border-radius: 50%;
        display: flex;
        justify-content: center;
        align-items: center;
        font-weight: 900;
        font-size: 1.2rem;
        box-shadow: 0 4px 10px rgba(0,0,0,0.3);

        &> img{
            width: 90%;
            border-radius: 100%;
        }
    }

    .logo-text-small {
        color: white;
        font-weight: 800;
        font-size: 1.4rem;
        letter-spacing: 1px;
        font-family: 'Montserrat', sans-serif;
        text-shadow: 0 2px 10px rgba(0,0,0,0.7);
    }

    .logo-btn:hover {
        transform: scale(1.05);
    }


    .nav-btn {
        display: flex;
        flex-direction: row;
        align-items: center;
        justify-content: center;
        gap: 6px;
        background-color: #e0f2fe;
        color: #0f172a;
        text-decoration: none;
        border: none;
        border-radius: 50px;
        padding: 10px 15px;
        min-width: 110px;
        height: 45px;
        font-weight: 700;
        font-size: 0.75rem;
        text-transform: uppercase;
        cursor: pointer;
        box-shadow: 0 4px 10px rgba(0,0,0,0.2);
        transition: all 0.2s ease;
        white-space: nowrap;
    }

    .nav-btn:hover {
        background-color: #ffffff;
        transform: translateY(3px);
        box-shadow: 0 6px 15px rgba(0,0,0,0.3);
    }


    .btn-icon {
        width: 20px;
        height: 20px;
        fill: currentColor;
    }


    @media (max-width: 600px) {
        .nav-btn span {
            display: none;
        }
        .nav-btn {
            min-width: auto;
            width: 50px;
            height: 50px;
            border-radius: 50%;
            padding: 0;
        }
        .level-high {
            margin-top: 25px;
        }
        .level-top {
            margin-top: 40px;
        }
        .navbar-container {
            height: 120px;
        }
    }
</style>

<div class="header-hero">

    <a href="${pageContext.request.contextPath}/view/index.jsp" class="top-logo">
        <div class="logo-circle-small">
            <img src="${pageContext.request.contextPath}/images/logox.jpeg">
        </div>
        <span class="logo-text-small">FANTAUNISA</span>
    </a>

    <%
        User user = (User) session.getAttribute("user");
        if (user!= null){
    %>
    <a href="${pageContext.request.contextPath}/LogoutServlet" class="auth-btn-top" style="background-color: #e74c3c;">
        ESCI <i class="fas fa-sign-out-alt"></i>
    </a>
    <%
        }else{
    %>
    <a href="${pageContext.request.contextPath}/view/login.jsp" class="auth-btn-top">
        ACCEDI / REGISTRATI <i class="fas fa-user"></i>
    </a>
    <%
        }
    %>

</div>

<div class="navbar-wrapper">
    <div class="navbar-container">

        <div class="navbar-bg"></div>

        <div class="navbar-grid">

            <a href="${pageContext.request.contextPath}/PostServlet" class="nav-btn level-low">
                <span>Community</span>
            </a>

            <a href="${pageContext.request.contextPath}/SquadServlet" class="nav-btn level-high">
                <span>Gestisci Rosa</span>
            </a>

            <%
                Role role = (Role) session.getAttribute("role");
                if (role==null || role==Role.FANTALLENATORE ){
            %>
            <a href="${pageContext.request.contextPath}/view/index.jsp" class="logo-btn level-top">
                <img class="palla" src="${pageContext.request.contextPath}/images/palla.png">
            </a>
            <%
            }else if(role==Role.GESTORE_UTENTI){
            %>
            <a href="${pageContext.request.contextPath}/ReportServlet" class="logo-btn level-top">
                <img class="palla" src="${pageContext.request.contextPath}/images/palla.png">
            </a>
            <%
            }else if(role==Role.GESTORE_DATI){
            %>
            <a href="${pageContext.request.contextPath}/view/admin_upload.jsp" class="logo-btn level-top">
                <img class="palla" src="${pageContext.request.contextPath}/images/palla.png">
            </a>
            <%
                }
            %>
            <a href="${pageContext.request.contextPath}/view/statistiche.jsp" class="nav-btn level-high">
                <span>Statistiche</span>
            </a>

            <a href="${pageContext.request.contextPath}/FormationServlet" class="nav-btn level-low">
                <span>Formazione</span>
            </a>

        </div>
    </div>
</div>