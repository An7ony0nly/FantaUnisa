document.addEventListener('DOMContentLoaded', function() {
    const uploadBtn = document.getElementById('uploadBtn');
    const progressContainer = document.getElementById('progressContainer');
    const progressFill = document.getElementById('progressFill');
    const previewContainer = document.getElementById('previewContainer');
    const closePreview = document.getElementById('closePreview');

    // Selezioniamo l'input file tramite il name="file" che è dentro il form
    const fileInput = document.querySelector('input[name="file"]');

    // Gestione click su Upload (che ora serve a generare l'ANTEPRIMA)
    uploadBtn.addEventListener('click', function(e) {
        // Fondamentale: previene il submit del form se per caso manca type="button"
        e.preventDefault();

        // Verifica se un file è stato selezionato
        if (!fileInput.files || fileInput.files.length === 0) {
            alert('Seleziona un file prima di effettuare l\'upload');
            return;
        }

        const selectedFile = fileInput.files[0];

        progressContainer.style.display = 'block';
        let progress = 5;

        // Simulazione barra di caricamento
        const interval = setInterval(function() {
            progress += Math.random() * 15;
            if (progress >= 100) {
                progress = 100;
                clearInterval(interval);

                // Al termine della barra, elaboriamo il file
                setTimeout(function() {
                    progressContainer.style.display = 'none';
                    progressFill.style.width = '0%';

                    // Chiamiamo la funzione per leggere e mostrare il file
                    processCSV(selectedFile);

                }, 500);
            }
            progressFill.style.width = progress + '%';
            document.querySelector('.progress-label').textContent = Math.floor(progress) + '% UPLOAD';
        }, 150);
    });

    // Funzione per leggere e renderizzare il CSV
    function processCSV(file) {
        const reader = new FileReader();

        // Usa 'ISO-8859-1' per supportare caratteri italiani se il CSV viene da Excel
        reader.readAsText(file, 'ISO-8859-1');

        reader.onload = function(event) {
            const csvText = event.target.result;
            const rows = csvText.split('\n');

            // Filtra righe vuote
            const cleanRows = rows.filter(r => r.trim() !== '');
            if (cleanRows.length === 0) return;

            // Prendi l'header e separa per punto e virgola
            const headers = cleanRows[0].split(';');

            // Costruzione HTML della tabella dinamica
            let htmlContent = `
            <div class="excel-preview">
                <div class="preview-header">
                  <span>Anteprima File: ${file.name}</span>
                  <span style="float: right">Record trovati: ${cleanRows.length - 1}</span>
                </div>
                <div class="excel-title">
                  ANTEPRIMA DATI DA IMPORTARE
                </div>
                
                <div style="overflow: auto; max-height: 500px; border: 1px solid #ddd;">
                    <table class="excel-table" style="width: 100%; border-collapse: collapse; font-family: Arial, sans-serif; font-size: 13px;">
                        <thead style="position: sticky; top: 0; z-index: 10;">
                        <tr>
                            <th style="background: #eee; padding: 5px; border: 1px solid #ccc; width: 30px;">#</th>`;

            headers.forEach(header => {
                htmlContent += `<th style="background: #4CAF50; color: white; padding: 10px; border: 1px solid #ccc; white-space: nowrap;">${header.trim()}</th>`;
            });

            htmlContent += `
                        </tr>
                        </thead>
                        <tbody>`;

            // Limitiamo l'anteprima alle prime 50 righe per performance se il file è enorme
            const previewLimit = Math.min(cleanRows.length, 100);

            for (let i = 1; i < previewLimit; i++) {
                const cells = cleanRows[i].split(';');
                if (cells.length > 1) {
                    htmlContent += `<tr>
                                    <td style="background: #f9f9f9; text-align: center; border: 1px solid #ddd;">${i}</td>`;

                    cells.forEach(cell => {
                        htmlContent += `<td style="padding: 6px; border: 1px solid #ddd; text-align: center;">${cell.trim()}</td>`;
                    });

                    htmlContent += `</tr>`;
                }
            }

            htmlContent += `
                        </tbody>
                    </table>
                </div>
                ${cleanRows.length > 100 ? '<div style="padding:10px; color: #666;"><i>Visualizzate prime 100 righe...</i></div>' : ''}
            </div>`;

            // Rimuovi la vecchia tabella statica/precedente
            const oldPreview = previewContainer.querySelector('.excel-preview');
            if (oldPreview) {
                oldPreview.remove();
            }

            // Inserisci l'HTML appena generato
            previewContainer.insertAdjacentHTML('beforeend', htmlContent);
            previewContainer.style.display = 'block';
        };

        reader.onerror = function() {
            alert("Errore nella lettura del file!");
        };
    }

    // Chiudi preview
    closePreview.addEventListener('click', function() {
        previewContainer.style.display = 'none';
    });
});

<!-- -->