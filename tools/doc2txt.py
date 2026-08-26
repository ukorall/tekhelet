"""חילוץ טקסט מ-Word 97-2003 (.doc) דרך טבלת ה-piece table.

LibreOffice בסביבה הזו מותקן בלי מסנני המסמכים, ולכן אין ברירה אלא לקרוא את
המבנה ישירות. ה-FIB מצביע על ה-CLX, שבתוכו PlcPcd - טבלה שאומרת לכל "חתיכה"
איפה היא יושבת ב-WordDocument ואם היא ANSI (cp1255) או UTF-16.
"""
import olefile, struct, sys

def extract(path):
    ole = olefile.OleFileIO(path)
    wd = ole.openstream('WordDocument').read()
    fib_flags = struct.unpack_from('<H', wd, 0x0A)[0]
    table_name = '1Table' if (fib_flags & 0x0200) else '0Table'
    if not ole.exists(table_name):
        table_name = '0Table' if ole.exists('0Table') else '1Table'
    tbl = ole.openstream(table_name).read()
    fcClx, lcbClx = struct.unpack_from('<II', wd, 0x01A2)
    clx = tbl[fcClx:fcClx+lcbClx]
    # מדלגים על ה-Prc-ים עד ל-Pcdt (סוג 2)
    i = 0
    while i < len(clx) and clx[i] == 1:
        cb = struct.unpack_from('<H', clx, i+1)[0]
        i += 3 + cb
    if i >= len(clx) or clx[i] != 2:
        raise ValueError('no Pcdt')
    lcb = struct.unpack_from('<I', clx, i+1)[0]
    plc = clx[i+5:i+5+lcb]
    n = (len(plc) - 4) // 12
    cps = [struct.unpack_from('<I', plc, k*4)[0] for k in range(n+1)]
    out = []
    for k in range(n):
        pcd = plc[(n+1)*4 + k*8:(n+1)*4 + k*8 + 8]
        fc = struct.unpack_from('<I', pcd, 2)[0]
        compressed = bool(fc & 0x40000000)
        fc &= 0x3FFFFFFF
        ncp = cps[k+1] - cps[k]
        if compressed:
            raw = wd[fc//2: fc//2 + ncp]
            out.append(raw.decode('cp1255', 'replace'))
        else:
            raw = wd[fc: fc + ncp*2]
            out.append(raw.decode('utf-16-le', 'replace'))
    ole.close()
    t = ''.join(out)
    return (t.replace('\r', '\n').replace('\x07', '\n')
             .replace('\x0b', '\n').replace('\x0c', '\n')
             .replace('\x13', '').replace('\x14', '').replace('\x15', ''))

if __name__ == '__main__':
    print(extract(sys.argv[1]))
