"""Genera el documento explicativo de tres paginas desde su fuente Markdown."""
from pathlib import Path
from html import escape
from reportlab.lib import colors
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.enums import TA_LEFT
from reportlab.lib.pagesizes import A4
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, PageBreak

repo = Path(__file__).resolve().parent.parent
source = (repo / "docs/DOCUMENTO_EXPLICATIVO.md").read_text(encoding="utf-8")
output = repo / "output/pdf/documento_explicativo.pdf"
output.parent.mkdir(parents=True, exist_ok=True)
styles = getSampleStyleSheet()
styles.add(ParagraphStyle(name="TituloProyecto", fontName="Helvetica-Bold", fontSize=19,
    leading=23, textColor=colors.HexColor("#143a52"), spaceAfter=14))
styles.add(ParagraphStyle(name="SeccionProyecto", fontName="Helvetica-Bold", fontSize=11,
    leading=14, textColor=colors.HexColor("#143a52"), spaceBefore=9, spaceAfter=5))
styles.add(ParagraphStyle(name="TextoProyecto", fontName="Helvetica", fontSize=10.1,
    leading=14.1, alignment=TA_LEFT, spaceAfter=7))

def decorar(canvas, doc):
    canvas.saveState()
    width, height = A4
    canvas.setStrokeColor(colors.HexColor("#238b91"))
    canvas.setLineWidth(2)
    canvas.line(48, height-37, width-48, height-37)
    canvas.setFont("Helvetica", 8)
    canvas.setFillColor(colors.HexColor("#526675"))
    canvas.drawString(48, 31, "Programacion II | Auditoria tecnica | 30/10/2026")
    canvas.drawRightString(width-48, 31, str(doc.page))
    canvas.restoreState()

story = []
for page_index, page in enumerate(source.split("<!-- PAGE -->")):
    if page_index:
        story.append(PageBreak())
    pending = []
    for line in page.strip().splitlines() + [""]:
        if line.startswith("#") or not line.strip():
            if pending:
                story.append(Paragraph(escape("\n".join(pending)).replace("\n", "<br/>"), styles["TextoProyecto"]))
                pending = []
        if line.startswith("# "):
            story.append(Paragraph(escape(line[2:]), styles["TituloProyecto"]))
        elif line.startswith("## "):
            story.append(Paragraph(escape(line[3:]), styles["SeccionProyecto"]))
        else:
            if line.strip():
                pending.append(line)
doc = SimpleDocTemplate(str(output), pagesize=A4, rightMargin=48, leftMargin=48,
    topMargin=52, bottomMargin=49, title="Sistema de Integracion y Analisis de Ventas",
    author="Jonathan Daniel Alberto Aguilar Ramirez y Luis Alberto Matta Rubio")
doc.build(story, onFirstPage=decorar, onLaterPages=decorar)
print(output)
