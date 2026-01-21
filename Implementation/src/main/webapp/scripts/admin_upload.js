document.addEventListener('DOMContentLoaded', function() {
    const uploadBtn = document.getElementById('uploadBtn');
    const progressContainer = document.getElementById('progressContainer');
    const progressFill = document.getElementById('progressFill');
    const previewContainer = document.getElementById('previewContainer');
    const closePreview = document.getElementById('closePreview');
    const spinner = document.querySelector('.spinner');

    // Selezioniamo l'input file tramite il name="file"
    const fileInput = document.querySelector('input[name="file"]');

    // Gestione click su Anteprima
    uploadBtn.addEventListener('click', function(e) {
        e.preventDefault();

        if (!fileInput.files || fileInput.files.length === 0) {
            alert('Seleziona un file prima di effettuare l\'upload');
            return;
        }

        const selectedFile = fileInput.files[0];

        // Mostra UI caricamento
        progressContainer.style.display = 'block';
        if(spinner) spinner.style.display = 'inline-block';

        let progress = 5;

        // Simulazione barra di caricamento
        const interval = setInterval(function() {
            progress += Math.random() * 15;
            if (progress >= 100) {
                progress = 100;
                clearInterval(interval);

                setTimeout(function() {
                    progressContainer.style.display = 'none';
                    if(spinner) spinner.style.display = 'none';
                    progressFill.style.width = '0%';

                    processCSV(selectedFile);

                }, 500);
            }
            progressFill.style.width = progress + '%';
            document.querySelector('.progress-label').textContent = Math.floor(progress) + '% ELABORAZIONE';
        }, 100);
    });

    // Funzione per leggere e renderizzare il CSV
    function processCSV(file) {
        const reader = new FileReader();

        reader.readAsText(file, 'ISO-8859-1');

        reader.onload = function(event) {
            const csvText = event.target.result;
            const rows = csvText.split('\n');

            const cleanRows = rows.filter(r => r.trim() !== '');
            if (cleanRows.length === 0) return;

            // Header del CSV
            const headers = cleanRows[0].split(';');

            // --- QUI HO CAMBIATO GLI STILI INLINE PER IL TEMA SCURO ---
            let htmlContent = `
            <div class="excel-preview">
                <div class="preview-header">
                  <span>Anteprima File: ${file.name}</span>
                  <span style="float: right">Record trovati: ${cleanRows.length - 1}</span>
                </div>
                <div class="excel-title">
                  ANTEPRIMA DATI DA IMPORTARE
                </div>
                
                <div style="overflow: auto; max-height: 500px; border: 1px solid rgba(255,255,255,0.1); border-radius: 8px;">
                    <table class="excel-table" style="width: 100%; border-collapse: collapse; font-family: 'Montserrat', sans-serif; font-size: 13px; color: #ecf0f1;">
                        <thead style="position: sticky; top: 0; z-index: 10;">
                        <tr>
                            <th style="background: #252B36; padding: 10px; border: 1px solid #34495e; width: 40px; color: #F58428;">#</th>`;

            headers.forEach(header => {
                // Header tabella verde scuro/professionale
                htmlContent += `<th style="background: #27ae60; color: white; padding: 12px; border: 1px solid #2ecc71; white-space: nowrap; text-transform: uppercase;">${header.trim()}</th>`;
            });

            htmlContent += `
                        </tr>
                        </thead>
                        <tbody>`;

            const previewLimit = Math.min(cleanRows.length, 100);

            for (let i = 1; i < previewLimit; i++) {
                const cells = cleanRows[i].split(';');
                if (cells.length > 1) {
                    // Righe alternate (simulazione) e sfondo scuro per le celle
                    let bgRow = i % 2 === 0 ? '#1C212E' : '#222838';

                    htmlContent += `<tr>
                                    <td style="background: ${bgRow}; text-align: center; border: 1px solid #34495e; color: #bdc3c7;">${i}</td>`;

                    cells.forEach(cell => {
                        htmlContent += `<td style="background: ${bgRow}; padding: 8px; border: 1px solid #34495e; text-align: center;">${cell.trim()}</td>`;
                    });

                    htmlContent += `</tr>`;
                }
            }

            htmlContent += `
                        </tbody>
                    </table>
                </div>
                ${cleanRows.length > 100 ? '<div style="padding:15px; color: #F58428; text-align:center; font-style:italic;">Visualizzate prime 100 righe...</div>' : ''}
            </div>`;

            // Aggiorna DOM
            const oldPreview = previewContainer.querySelector('.excel-preview');
            if (oldPreview) oldPreview.remove();

            previewContainer.insertAdjacentHTML('beforeend', htmlContent);
            previewContainer.style.display = 'block';

            // Scroll automatico alla preview
            previewContainer.scrollIntoView({ behavior: 'smooth' });
        };

        reader.onerror = function() {
            alert("Errore nella lettura del file!");
        };
    }

    closePreview.addEventListener('click', function() {
        previewContainer.style.display = 'none';
    });
});