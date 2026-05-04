import zipfile
from pathlib import Path
from xml.sax.saxutils import escape


ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "UTS_Distributed_System_Chat_Presentation.pptx"

SLIDES = [
    ("Multi-Client Chat Application", [
        "Using Java Socket Programming",
        "Mata Kuliah: Distributed System",
        "Kelompok: Halimatuz Z dan anggota"
    ]),
    ("Tujuan Project", [
        "Membuat aplikasi chat dengan arsitektur client-server.",
        "Server menangani banyak client secara bersamaan.",
        "Pesan diterima client lain secara real-time.",
        "Client memakai GUI sederhana dan mudah digunakan."
    ]),
    ("Teknologi", [
        "Java JDK 8 atau lebih baru.",
        "ServerSocket dan Socket dari java.net.",
        "BufferedReader dan BufferedWriter dari java.io.",
        "javax.swing untuk user interface client.",
        "Multithreading untuk simultaneous clients."
    ]),
    ("Arsitektur Sistem", [
        "Client GUI membuat koneksi Socket ke server.",
        "ServerSocket menerima koneksi pada port 5000.",
        "Setiap client ditangani oleh ClientHandler thread.",
        "Server membroadcast pesan ke semua client lain."
    ]),
    ("Alur Server", [
        "Server membuka port 5000.",
        "Server menunggu koneksi dengan accept().",
        "Client baru ditambahkan ke daftar clients.",
        "ClientHandler membaca pesan dari client.",
        "Server meneruskan pesan ke client lain."
    ]),
    ("Alur Client", [
        "Client mengisi host, port, dan username.",
        "Client tersambung ke server dengan Socket.",
        "User mengetik pesan pada text field.",
        "Tombol Send atau Enter mengirim pesan.",
        "Listener thread menerima pesan dari server."
    ]),
    ("Struktur Kode", [
        "ChatServer.java: server, daftar client, dan broadcast.",
        "ClientHandler: thread untuk setiap client.",
        "ChatClient.java: GUI Swing, send, dan receive.",
        "compile.bat, run-server.bat, run-client.bat untuk menjalankan demo."
    ]),
    ("User Interface", [
        "Conversation history memakai JTextArea.",
        "Input pesan memakai JTextField.",
        "Tombol Send memakai JButton.",
        "Auto-scroll ke pesan terbaru.",
        "Status koneksi ditampilkan di bagian atas."
    ]),
    ("Skenario Demo", [
        "Jalankan compile.bat.",
        "Jalankan run-server.bat.",
        "Jalankan run-client.bat beberapa kali.",
        "Gunakan username berbeda.",
        "Kirim pesan dan lihat broadcast pada client lain."
    ]),
    ("Kesimpulan", [
        "Aplikasi menerapkan distributed system sederhana.",
        "Komunikasi dilakukan dengan socket.",
        "Multithreading membuat server melayani banyak client.",
        "GUI Swing membuat aplikasi mudah digunakan."
    ]),
]


def content_types():
    overrides = "\n".join(
        f'<Override PartName="/ppt/slides/slide{i}.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slide+xml"/>'
        for i in range(1, len(SLIDES) + 1)
    )
    return f'''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/ppt/presentation.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.presentation.main+xml"/>
  <Override PartName="/ppt/slideMasters/slideMaster1.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slideMaster+xml"/>
  <Override PartName="/ppt/slideLayouts/slideLayout1.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slideLayout+xml"/>
  <Override PartName="/ppt/theme/theme1.xml" ContentType="application/vnd.openxmlformats-officedocument.theme+xml"/>
  <Override PartName="/docProps/core.xml" ContentType="application/vnd.openxmlformats-package.core-properties+xml"/>
  <Override PartName="/docProps/app.xml" ContentType="application/vnd.openxmlformats-officedocument.extended-properties+xml"/>
  {overrides}
</Types>'''


def root_rels():
    return '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="ppt/presentation.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/>
  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties" Target="docProps/app.xml"/>
</Relationships>'''


def presentation_xml():
    slide_ids = "\n".join(
        f'<p:sldId id="{255 + i}" r:id="rId{i}"/>'
        for i in range(1, len(SLIDES) + 1)
    )
    return f'''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:presentation xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main">
  <p:sldMasterIdLst><p:sldMasterId id="2147483648" r:id="rId{len(SLIDES) + 1}"/></p:sldMasterIdLst>
  <p:sldIdLst>{slide_ids}</p:sldIdLst>
  <p:sldSz cx="12192000" cy="6858000" type="wide"/>
  <p:notesSz cx="6858000" cy="9144000"/>
</p:presentation>'''


def presentation_rels():
    rels = "\n".join(
        f'<Relationship Id="rId{i}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slide" Target="slides/slide{i}.xml"/>'
        for i in range(1, len(SLIDES) + 1)
    )
    rels += f'\n<Relationship Id="rId{len(SLIDES) + 1}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster" Target="slideMasters/slideMaster1.xml"/>'
    return f'''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">{rels}</Relationships>'''


def slide_xml(title, bullets):
    bullet_runs = []
    y = 1850000
    for bullet in bullets:
        bullet_runs.append(f'''
    <p:sp>
      <p:nvSpPr><p:cNvPr id="{10 + len(bullet_runs)}" name="Bullet"/><p:cNvSpPr/><p:nvPr/></p:nvSpPr>
      <p:spPr><a:xfrm><a:off x="1150000" y="{y}"/><a:ext cx="9800000" cy="430000"/></a:xfrm><a:prstGeom prst="rect"><a:avLst/></a:prstGeom></p:spPr>
      <p:txBody><a:bodyPr/><a:lstStyle/><a:p><a:pPr marL="342900" indent="-228600"><a:buChar char="•"/></a:pPr><a:r><a:rPr lang="id-ID" sz="2350"/><a:t>{escape(bullet)}</a:t></a:r></a:p></p:txBody>
    </p:sp>''')
        y += 560000

    return f'''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sld xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main">
  <p:cSld>
    <p:bg><p:bgPr><a:solidFill><a:srgbClr val="F8FAFC"/></a:solidFill><a:effectLst/></p:bgPr></p:bg>
    <p:spTree>
      <p:nvGrpSpPr><p:cNvPr id="1" name=""/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr>
      <p:grpSpPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="0" cy="0"/><a:chOff x="0" y="0"/><a:chExt cx="0" cy="0"/></a:xfrm></p:grpSpPr>
      <p:sp>
        <p:nvSpPr><p:cNvPr id="2" name="Title"/><p:cNvSpPr/><p:nvPr/></p:nvSpPr>
        <p:spPr><a:xfrm><a:off x="850000" y="620000"/><a:ext cx="10500000" cy="850000"/></a:xfrm><a:prstGeom prst="rect"><a:avLst/></a:prstGeom></p:spPr>
        <p:txBody><a:bodyPr/><a:lstStyle/><a:p><a:r><a:rPr lang="id-ID" sz="4050" b="1"><a:solidFill><a:srgbClr val="0F172A"/></a:solidFill></a:rPr><a:t>{escape(title)}</a:t></a:r></a:p></p:txBody>
      </p:sp>
      {''.join(bullet_runs)}
    </p:spTree>
  </p:cSld>
  <p:clrMapOvr><a:masterClrMapping/></p:clrMapOvr>
</p:sld>'''


def empty_rels():
    return '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"/>'''


def slide_master():
    return '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sldMaster xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main">
  <p:cSld><p:spTree><p:nvGrpSpPr><p:cNvPr id="1" name=""/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr><p:grpSpPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="0" cy="0"/><a:chOff x="0" y="0"/><a:chExt cx="0" cy="0"/></a:xfrm></p:grpSpPr></p:spTree></p:cSld>
  <p:clrMap bg1="lt1" tx1="dk1" bg2="lt2" tx2="dk2" accent1="accent1" accent2="accent2" accent3="accent3" accent4="accent4" accent5="accent5" accent6="accent6" hlink="hlink" folHlink="folHlink"/>
  <p:sldLayoutIdLst><p:sldLayoutId id="2147483649" r:id="rId1"/></p:sldLayoutIdLst>
</p:sldMaster>'''


def slide_layout():
    return '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sldLayout xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main" type="blank" preserve="1">
  <p:cSld name="Blank"><p:spTree><p:nvGrpSpPr><p:cNvPr id="1" name=""/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr><p:grpSpPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="0" cy="0"/><a:chOff x="0" y="0"/><a:chExt cx="0" cy="0"/></a:xfrm></p:grpSpPr></p:spTree></p:cSld>
  <p:clrMapOvr><a:masterClrMapping/></p:clrMapOvr>
</p:sldLayout>'''


def theme():
    return '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<a:theme xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" name="Simple">
  <a:themeElements>
    <a:clrScheme name="Simple"><a:dk1><a:srgbClr val="111827"/></a:dk1><a:lt1><a:srgbClr val="FFFFFF"/></a:lt1><a:dk2><a:srgbClr val="1F2937"/></a:dk2><a:lt2><a:srgbClr val="F8FAFC"/></a:lt2><a:accent1><a:srgbClr val="2563EB"/></a:accent1><a:accent2><a:srgbClr val="059669"/></a:accent2><a:accent3><a:srgbClr val="DC2626"/></a:accent3><a:accent4><a:srgbClr val="D97706"/></a:accent4><a:accent5><a:srgbClr val="7C3AED"/></a:accent5><a:accent6><a:srgbClr val="0891B2"/></a:accent6><a:hlink><a:srgbClr val="2563EB"/></a:hlink><a:folHlink><a:srgbClr val="7C3AED"/></a:folHlink></a:clrScheme>
    <a:fontScheme name="Simple"><a:majorFont><a:latin typeface="Aptos Display"/></a:majorFont><a:minorFont><a:latin typeface="Aptos"/></a:minorFont></a:fontScheme>
    <a:fmtScheme name="Simple"><a:fillStyleLst><a:solidFill><a:schemeClr val="phClr"/></a:solidFill></a:fillStyleLst><a:lnStyleLst><a:ln w="9525"><a:solidFill><a:schemeClr val="phClr"/></a:solidFill></a:ln></a:lnStyleLst><a:effectStyleLst><a:effectStyle><a:effectLst/></a:effectStyle></a:effectStyleLst><a:bgFillStyleLst><a:solidFill><a:schemeClr val="phClr"/></a:solidFill></a:bgFillStyleLst></a:fmtScheme>
  </a:themeElements>
</a:theme>'''


def app_props():
    return '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties" xmlns:vt="http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes"><Application>Codex</Application><Slides>10</Slides></Properties>'''


def core_props():
    return '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<cp:coreProperties xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:dcmitype="http://purl.org/dc/dcmitype/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"><dc:title>UTS Distributed System Chat Presentation</dc:title><dc:creator>Codex</dc:creator></cp:coreProperties>'''


def main():
    with zipfile.ZipFile(OUT, "w", zipfile.ZIP_DEFLATED) as ppt:
        ppt.writestr("[Content_Types].xml", content_types())
        ppt.writestr("_rels/.rels", root_rels())
        ppt.writestr("ppt/presentation.xml", presentation_xml())
        ppt.writestr("ppt/_rels/presentation.xml.rels", presentation_rels())
        ppt.writestr("ppt/slideMasters/slideMaster1.xml", slide_master())
        ppt.writestr("ppt/slideMasters/_rels/slideMaster1.xml.rels", '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout" Target="../slideLayouts/slideLayout1.xml"/></Relationships>''')
        ppt.writestr("ppt/slideLayouts/slideLayout1.xml", slide_layout())
        ppt.writestr("ppt/slideLayouts/_rels/slideLayout1.xml.rels", empty_rels())
        ppt.writestr("ppt/theme/theme1.xml", theme())
        ppt.writestr("docProps/app.xml", app_props())
        ppt.writestr("docProps/core.xml", core_props())
        for i, (title, bullets) in enumerate(SLIDES, 1):
            ppt.writestr(f"ppt/slides/slide{i}.xml", slide_xml(title, bullets))
            ppt.writestr(f"ppt/slides/_rels/slide{i}.xml.rels", empty_rels())
    print(OUT)


if __name__ == "__main__":
    main()
