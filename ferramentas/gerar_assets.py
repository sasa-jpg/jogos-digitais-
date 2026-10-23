"""
Gera os tiles de chao, as decoracoes e os efeitos do fundo (cidade, floresta e Alpes).
Uso (na raiz do projeto):  python3 ferramentas/gerar_assets.py
Requer: pip install pillow
"""
import math, random, os
from PIL import Image, ImageDraw, ImageFilter, ImageChops

W, H = 960, 720
ESQ, DIR = 170, 790
S = 4  # supersampling dos sprites
os.makedirs("assets/fundo", exist_ok=True); os.makedirs("assets/decor", exist_ok=True); os.makedirs("assets/vivo", exist_ok=True)
D = "assets/decor/"; F = "assets/fundo/"; V = "assets/vivo/"

# ---------------------------------------------------------------- utilidades
def wrap_draw(fn, y, ext):
    for off in (-H, 0, H):
        if -ext <= y + off <= H + ext:
            fn(y + off)

def blot(d, x, y, rx, ry, cor):
    wrap_draw(lambda yy: d.ellipse([x-rx, yy-ry, x+rx, yy+ry], fill=cor), y, ry)

def vary(c, v):
    k = random.randint(-v, v)
    return tuple(max(0, min(255, a + k)) for a in c[:3]) + tuple(c[3:])

def mix(a, b, t):
    return tuple(int(a[i]*(1-t) + b[i]*t) for i in range(3))

def escurece(c, v): return tuple(max(0, x-v) for x in c[:3])
def clareia(c, v): return tuple(min(255, x+v) for x in c[:3])

def novo(w, h): return Image.new("RGBA", (w*S, h*S), (0, 0, 0, 0))
def fim(im, w, h): return im.resize((w, h), Image.LANCZOS)

def sombra_elipse(d, cx, cy, rx, ry, a=70):
    d.ellipse([(cx-rx)*S, (cy-ry)*S, (cx+rx)*S, (cy+ry)*S], fill=(0, 0, 0, a))

def copa(d, cx, cy, r, base, claro, escuro, n=14, seed=None):
    rnd = random.Random(seed if seed is not None else int(cx*7+cy))
    d.ellipse([(cx-r)*S, (cy-r)*S, (cx+r)*S, (cy+r)*S], fill=escuro)
    for i in range(n):
        a = rnd.random()*6.283; rr = rnd.random()*r*0.55
        x, y = cx + math.cos(a)*rr, cy + math.sin(a)*rr - r*0.08
        q = r*(0.38 + rnd.random()*0.2)
        d.ellipse([(x-q)*S, (y-q)*S, (x+q)*S, (y+q)*S], fill=base)
    for i in range(n//2):
        a = rnd.random()*6.283; rr = rnd.random()*r*0.45
        x, y = cx + math.cos(a)*rr - r*0.1, cy + math.sin(a)*rr - r*0.25
        q = r*0.22
        d.ellipse([(x-q)*S, (y-q)*S, (x+q)*S, (y+q)*S], fill=claro)

# periodico: desenha em 3 copias verticais, aplica filtro e recorta o miolo (sem emenda)
def tile_suave(desenhar, raio):
    big = Image.new("RGB", (W, H*3))
    desenhar(big, ImageDraw.Draw(big))
    big = big.filter(ImageFilter.GaussianBlur(raio))
    return big.crop((0, H, W, H*2))

# ================================================================ CIDADE (mantida)
def tile_cidade():
    random.seed(7)
    im = Image.new("RGB", (W, H), (205, 196, 190)); d = ImageDraw.Draw(im)
    for x0, x1 in ((0, 150), (810, W)):
        for y in range(0, H, 60):
            for x in range(x0, x1, 50):
                off = 25 if (y // 60) % 2 else 0
                d.rectangle([x+off, y, x+off+48, y+58], fill=vary((206, 197, 191), 8), outline=(176, 166, 162))
    d.rectangle([150, 0, 170, H], fill=(150, 142, 140)); d.rectangle([790, 0, 810, H], fill=(150, 142, 140))
    d.rectangle([166, 0, 170, H], fill=(110, 104, 104)); d.rectangle([790, 0, 794, H], fill=(110, 104, 104))
    d.rectangle([170, 0, 790, H], fill=(74, 74, 84))
    for _ in range(1400):
        blot(d, random.randint(172, 788), random.randint(0, H-1), random.randint(1, 3), random.randint(1, 3), vary((74, 74, 84), 14))
    for _ in range(14):
        blot(d, random.randint(190, 770), random.randint(0, H-1), random.randint(10, 26), random.randint(3, 6), (64, 64, 74))
    d.rectangle([182, 0, 188, H], fill=(225, 225, 225)); d.rectangle([772, 0, 778, H], fill=(225, 225, 225))
    for y in range(0, H, 120):
        d.rectangle([475, y+10, 485, y+70], fill=(238, 238, 238))
    for cx, cy in ((330, 210), (630, 520)):
        blot(d, cx, cy, 26, 26, (52, 52, 58)); blot(d, cx, cy, 20, 20, (88, 88, 96)); blot(d, cx, cy, 13, 13, (60, 60, 68))
    return im

def arvore(nome, w=120, h=130, base=(70, 160, 50), claro=(130, 205, 70), escuro=(40, 105, 40), n=14, tronco=(120, 76, 38)):
    im = novo(w, h); d = ImageDraw.Draw(im)
    sombra_elipse(d, w/2+8, h-14, w*0.32, 11)
    d.rectangle([(w/2-7)*S, (h*0.55)*S, (w/2+7)*S, (h-12)*S], fill=tronco)
    copa(d, w/2, h*0.42, w*0.44, base, claro, escuro, n=n)
    fim(im, w, h).save(nome)

def poste(nome, w=44, h=84):
    im = novo(w, h); d = ImageDraw.Draw(im)
    sombra_elipse(d, w/2+6, h-6, 12, 5)
    d.rectangle([(w/2-3)*S, 14*S, (w/2+3)*S, (h-8)*S], fill=(28, 34, 62))
    d.ellipse([(w/2-9)*S, (h-14)*S, (w/2+9)*S, (h-2)*S], fill=(30, 36, 66))
    d.rounded_rectangle([(w/2-10)*S, 4*S, (w/2+10)*S, 18*S], radius=5*S, fill=(36, 44, 80))
    d.ellipse([(w/2-6)*S, 8*S, (w/2+6)*S, 16*S], fill=(255, 232, 150))
    fim(im, w, h).save(nome)

def hidrante(nome, w=34, h=46):
    im = novo(w, h); d = ImageDraw.Draw(im)
    sombra_elipse(d, w/2+5, h-5, 12, 5)
    d.rounded_rectangle([8*S, 14*S, (w-8)*S, (h-6)*S], radius=4*S, fill=(190, 40, 36))
    d.ellipse([6*S, 6*S, (w-6)*S, 22*S], fill=(205, 50, 44))
    d.rectangle([2*S, 22*S, (w-2)*S, 28*S], fill=(170, 34, 30))
    d.rectangle([(w/2-3)*S, 2*S, (w/2+3)*S, 8*S], fill=(150, 30, 28))
    fim(im, w, h).save(nome)

def banco(nome, w=46, h=86):
    im = novo(w, h); d = ImageDraw.Draw(im)
    sombra_elipse(d, w/2+5, h-6, 18, 6)
    d.rectangle([4*S, 6*S, (w-6)*S, (h-10)*S], fill=(140, 92, 50))
    for y in range(10, h-12, 10):
        d.line([4*S, y*S, (w-6)*S, y*S], fill=(100, 64, 34), width=2*S)
    d.rectangle([(w-10)*S, 6*S, (w-4)*S, (h-10)*S], fill=(80, 52, 30))
    fim(im, w, h).save(nome)

def vaso(nome, w=60, h=60):
    im = novo(w, h); d = ImageDraw.Draw(im)
    sombra_elipse(d, w/2+4, h-6, 22, 6)
    d.rectangle([8*S, 30*S, (w-8)*S, (h-8)*S], fill=(96, 100, 112))
    copa(d, w/2, 26, 22, (80, 170, 60), (240, 110, 150), (45, 115, 45), n=9)
    fim(im, w, h).save(nome)

# ================================================================ FLORESTA
def tile_floresta():
    random.seed(11)
    musgo = (46, 104, 46)
    im = Image.new("RGB", (W, H), musgo); d = ImageDraw.Draw(im)
    # grama em varias camadas de verde
    for _ in range(700):
        blot(d, random.randint(0, W), random.randint(0, H-1), random.randint(10, 34), random.randint(8, 24),
             random.choice([(38, 92, 44), (54, 120, 50), (62, 132, 54), (34, 84, 48), (70, 140, 58)]))
    def borda(y, lado):
        k = 2*math.pi/H
        base = 172 if lado == 0 else 788
        return base + 13*math.sin(k*2*y + (0.7 if lado else 0)) + 8*math.sin(k*5*y + 1.3) + 4*math.sin(k*11*y)
    esq = [(borda(y, 0), y) for y in range(-20, H+21, 5)]
    dire = [(borda(y, 1), y) for y in range(H+20, -21, -5)]
    # faixa de terra escura (sombra) e terra
    d.polygon([(x-14, y) for x, y in esq] + [(x+14, y) for x, y in dire], fill=(60, 78, 40))
    d.polygon([(x-5, y) for x, y in esq] + [(x+5, y) for x, y in dire], fill=(150, 96, 52))
    d.polygon(esq + dire, fill=(196, 132, 68))
    # centro mais pisado / claro
    cl = [(borda(y, 0)+70, y) for y in range(-20, H+21, 5)]; cd = [(borda(y, 1)-70, y) for y in range(H+20, -21, -5)]
    d.polygon(cl + cd, fill=(206, 142, 76))
    for _ in range(1000):
        y = random.randint(0, H-1)
        blot(d, random.randint(int(borda(y, 0))+6, int(borda(y, 1))-6), y, random.randint(2, 7), random.randint(2, 4),
             vary((198, 134, 70), 18))
    # musgo invadindo a borda da trilha
    for _ in range(60):
        y = random.randint(0, H-1); lado = random.randint(0, 1)
        x = borda(y, lado) + (random.randint(4, 24) if lado == 0 else -random.randint(4, 24))
        blot(d, x, y, random.randint(5, 12), random.randint(4, 9), random.choice([(70, 130, 52), (88, 150, 60)]))
    # manchas de luz (clareira) e sombra de copas
    for _ in range(12):
        y = random.randint(0, H-1)
        blot(d, random.randint(int(borda(y, 0))+30, int(borda(y, 1))-30), y, random.randint(26, 60), random.randint(10, 22), (222, 164, 92))
    for _ in range(14):
        y = random.randint(0, H-1)
        blot(d, random.randint(int(borda(y, 0))+10, int(borda(y, 1))-10), y, random.randint(20, 46), random.randint(9, 18), (160, 98, 52))
    # raizes cruzando a borda
    for _ in range(9):
        y = random.randint(20, H-20); lado = random.randint(0, 1)
        x0 = borda(y, lado); sinal = 1 if lado == 0 else -1
        pts = [(x0 - sinal*10, y)]
        for i in range(1, 5):
            pts.append((x0 + sinal*i*11, y + 7*math.sin(i*1.3 + y)))
        for off in (-H, 0, H):
            d.line([(px, py+off) for px, py in pts], fill=(110, 68, 34), width=6)
            d.line([(px, py+off-2) for px, py in pts], fill=(150, 98, 50), width=2)
    # folhas secas
    for _ in range(170):
        y = random.randint(0, H-1); x = random.randint(40, W-40)
        cor = random.choice([(222, 150, 40), (190, 80, 32), (236, 190, 60), (150, 60, 30)])
        blot(d, x, y, random.randint(2, 4), random.randint(1, 3), cor)
    # pedrinhas e flores
    for _ in range(26):
        y = random.randint(0, H-1); x = random.randint(int(borda(y, 0))+10, int(borda(y, 1))-10)
        blot(d, x, y, 5, 4, (96, 90, 84)); blot(d, x, y-1, 4, 3, (150, 146, 140))
    for _ in range(120):
        y = random.randint(0, H-1)
        x = random.choice([random.randint(5, int(borda(y, 0))-20), random.randint(int(borda(y, 1))+20, W-5)])
        blot(d, x, y, 2, 2, random.choice([(250, 250, 240), (244, 150, 190), (250, 220, 90), (190, 150, 240)]))
    return im

def arvore_grande(nome, w=190, h=200):
    im = novo(w, h); d = ImageDraw.Draw(im)
    sombra_elipse(d, w/2+14, h-18, w*0.34, 14, 85)
    # raizes + tronco
    for lado in (-1, 1):
        d.polygon([((w/2)*S, (h-38)*S), ((w/2+lado*26)*S, (h-12)*S), ((w/2+lado*10)*S, (h-12)*S), ((w/2)*S, (h-28)*S)], fill=(98, 62, 32))
    d.rectangle([(w/2-12)*S, (h*0.5)*S, (w/2+12)*S, (h-22)*S], fill=(112, 70, 36))
    d.rectangle([(w/2-12)*S, (h*0.5)*S, (w/2-4)*S, (h-22)*S], fill=(86, 54, 28))
    copa(d, w/2, h*0.38, w*0.47, (40, 118, 56), (96, 178, 70), (22, 76, 44), n=22, seed=5)
    copa(d, w/2+20, h*0.30, w*0.26, (50, 134, 60), (120, 196, 76), (28, 90, 46), n=8, seed=9)
    fim(im, w, h).save(nome)

def pinheiro(nome, w=70, h=120, tons=((30, 110, 50), (36, 128, 56), (46, 146, 62)), neve=False):
    im = novo(w, h); d = ImageDraw.Draw(im)
    sombra_elipse(d, w/2+6, h-8, w*0.3, 8, 85 if neve else 70)
    d.rectangle([(w/2-5)*S, (h-30)*S, (w/2+5)*S, (h-8)*S], fill=(100, 64, 34))
    camadas = [(4, 48, w*0.29), (26, 78, w*0.39), (50, 106, w*0.47)]
    for (top, bot, half), cor in zip(camadas, tons):
        d.polygon([(w/2*S, top*S), ((w/2-half)*S, bot*S), ((w/2+half)*S, bot*S)], fill=cor)
        d.polygon([(w/2*S, top*S), (w/2*S, bot*S), ((w/2+half)*S, bot*S)], fill=escurece(cor, 24))
        if neve:  # capa de neve em cada andar + borda irregular
            alt = (bot-top)*0.55
            d.polygon([(w/2*S, top*S), ((w/2-half*0.62)*S, (top+alt)*S), ((w/2-half*0.2)*S, (top+alt*0.78)*S),
                       ((w/2+half*0.1)*S, (top+alt*0.95)*S), ((w/2+half*0.62)*S, (top+alt)*S)], fill=(246, 250, 255))
            d.polygon([(w/2*S, top*S), ((w/2+half*0.62)*S, (top+alt)*S), ((w/2+half*0.1)*S, (top+alt*0.95)*S), (w/2*S, (top+alt*0.6)*S)], fill=(205, 222, 242))
    fim(im, w, h).save(nome)

def samambaia(nome, w=84, h=72):
    im = novo(w, h); d = ImageDraw.Draw(im)
    sombra_elipse(d, w/2+4, h-8, w*0.34, 6)
    cx, cy = w/2, h-12
    for ang in range(-80, 81, 32):
        a = math.radians(ang - 90)
        L = h*0.78 * (0.8 + 0.2*math.cos(math.radians(ang)))
        ex, ey = cx + math.cos(a)*L, cy + math.sin(a)*L
        d.line([(cx*S, cy*S), (ex*S, ey*S)], fill=(34, 98, 46), width=2*S)
        for t in range(2, 11):
            f = t/11; px, py = cx + (ex-cx)*f, cy + (ey-cy)*f
            tam = (1-f)*9 + 2
            for lado in (-1, 1):
                px2 = px + math.cos(a + lado*1.2)*tam; py2 = py + math.sin(a + lado*1.2)*tam
                d.line([(px*S, py*S), (px2*S, py2*S)], fill=(62, 150, 62) if t % 2 else (84, 172, 70), width=2*S)
    fim(im, w, h).save(nome)

def cogumelos(nome, w=60, h=50):
    im = novo(w, h); d = ImageDraw.Draw(im)
    sombra_elipse(d, w/2+3, h-7, 22, 6)
    for cx, cy, r in ((18, 30, 11), (36, 26, 14), (46, 36, 8)):
        d.rectangle([(cx-3)*S, cy*S, (cx+3)*S, (cy+14)*S], fill=(240, 228, 200))
        d.ellipse([(cx-r)*S, (cy-r*0.7)*S, (cx+r)*S, (cy+r*0.45)*S], fill=(205, 48, 40))
        d.ellipse([(cx-r*0.55)*S, (cy-r*0.55)*S, (cx-r*0.15)*S, (cy-r*0.2)*S], fill=(250, 244, 236))
        d.ellipse([(cx+r*0.2)*S, (cy-r*0.4)*S, (cx+r*0.6)*S, (cy-r*0.05)*S], fill=(250, 244, 236))
    fim(im, w, h).save(nome)

def tronco_caido(nome, w=124, h=48):
    im = novo(w, h); d = ImageDraw.Draw(im)
    sombra_elipse(d, w/2+4, h-8, w*0.42, 7, 80)
    d.rounded_rectangle([14*S, 8*S, (w-8)*S, (h-12)*S], radius=10*S, fill=(116, 72, 36))
    d.rounded_rectangle([14*S, 8*S, (w-8)*S, 20*S], radius=8*S, fill=(142, 92, 48))
    for x in range(30, w-14, 18):
        d.line([x*S, 12*S, (x+4)*S, (h-14)*S], fill=(86, 54, 28), width=S)
    d.ellipse([4*S, 6*S, 26*S, (h-10)*S], fill=(206, 156, 98)); d.ellipse([9*S, 12*S, 21*S, (h-16)*S], fill=(176, 126, 74))
    for cx in (50, 78, 100):
        d.ellipse([(cx-12)*S, 6*S, (cx+12)*S, 17*S], fill=(70, 140, 54))   # musgo
    fim(im, w, h).save(nome)

def pedra_musgo(nome, w=88, h=74):
    im = novo(w, h); d = ImageDraw.Draw(im)
    sombra_elipse(d, w/2+6, h-8, w*0.42, 9)
    pts = [(0.08, 0.80), (0.04, 0.45), (0.28, 0.12), (0.65, 0.06), (0.96, 0.42), (0.92, 0.80), (0.55, 0.92)]
    P = [(x*w*S, y*(h-8)*S) for x, y in pts]
    d.polygon(P, fill=(128, 132, 140))
    d.polygon([P[1], P[2], P[3], (w*0.45*S, h*0.45*S)], fill=(160, 164, 172))
    d.polygon([P[4], P[5], P[6], (w*0.5*S, h*0.5*S)], fill=(94, 98, 108))
    for mx, my, mr in ((0.30, 0.26, 0.20), (0.52, 0.17, 0.22), (0.76, 0.30, 0.16), (0.42, 0.40, 0.12), (0.18, 0.48, 0.09)):
        d.ellipse([(mx-mr)*w*S, (my-mr*0.8)*h*S, (mx+mr)*w*S, (my+mr*0.8)*h*S], fill=(72, 146, 56))
        d.ellipse([(mx-mr*0.6)*w*S, (my-mr*0.7)*h*S, (mx+mr*0.3)*w*S, (my+mr*0.1)*h*S], fill=(108, 176, 70))
    d.line(P + [P[0]], fill=(70, 74, 82), width=2*S)
    fim(im, w, h).save(nome)

def flores(nome, w=64, h=46):
    im = novo(w, h); d = ImageDraw.Draw(im)
    sombra_elipse(d, w/2+3, h-6, 24, 5)
    rnd = random.Random(3)
    for _ in range(9):
        x, y = rnd.randint(10, w-10), rnd.randint(12, h-14)
        d.line([x*S, (y+4)*S, x*S, (h-8)*S], fill=(50, 120, 52), width=S)
        d.ellipse([(x-6)*S, (h-18)*S, (x+6)*S, (h-8)*S], fill=(60, 140, 58))
        cor = rnd.choice([(250, 250, 244), (246, 150, 196), (252, 222, 90), (196, 150, 244)])
        for a in range(5):
            ang = a*1.2566
            d.ellipse([(x+math.cos(ang)*3-2.5)*S, (y+math.sin(ang)*3-2.5)*S, (x+math.cos(ang)*3+2.5)*S, (y+math.sin(ang)*3+2.5)*S], fill=cor)
        d.ellipse([(x-1.6)*S, (y-1.6)*S, (x+1.6)*S, (y+1.6)*S], fill=(250, 200, 60))
    fim(im, w, h).save(nome)

def arbusto(nome, w=70, h=60, base=(80, 170, 60), claro=(150, 215, 80), escuro=(45, 115, 45)):
    im = novo(w, h); d = ImageDraw.Draw(im)
    sombra_elipse(d, w/2+5, h-8, w*0.38, 8)
    copa(d, w/2, h*0.48, w*0.42, base, claro, escuro, n=10)
    fim(im, w, h).save(nome)

def toco(nome, w=60, h=64):
    im = novo(w, h); d = ImageDraw.Draw(im)
    sombra_elipse(d, w/2+5, h-6, w*0.4, 7)
    d.rectangle([8*S, 22*S, (w-8)*S, (h-12)*S], fill=(122, 76, 38))
    d.ellipse([8*S, (h-20)*S, (w-8)*S, (h-4)*S], fill=(122, 76, 38))
    d.ellipse([8*S, 14*S, (w-8)*S, 34*S], fill=(196, 144, 86))
    d.ellipse([16*S, 18*S, (w-16)*S, 30*S], fill=(170, 118, 66))
    d.ellipse([22*S, 21*S, (w-22)*S, 27*S], fill=(150, 100, 56))
    fim(im, w, h).save(nome)

# ================================================================ ALPES (neve)
def tile_alpes():
    random.seed(21)
    NEVE = (238, 244, 252)
    # base: neve com ondulacoes azuladas suaves (periodico + blur)
    def base(big, d):
        d.rectangle([0, 0, W, H*3], fill=NEVE)
        for _ in range(260):
            x = random.randint(0, W); y = random.randint(0, H-1)
            cor = random.choice([(206, 222, 242), (222, 234, 250), (252, 253, 255), (196, 214, 238), (244, 249, 255)])
            rx, ry = random.randint(30, 90), random.randint(12, 34)
            for k in range(3):
                d.ellipse([x-rx, y+k*H-ry, x+rx, y+k*H+ry], fill=cor)
    im = tile_suave(base, 9)
    d = ImageDraw.Draw(im)
    def borda(y, lado):
        k = 2*math.pi/H
        b = 176 if lado == 0 else 784
        return b + 14*math.sin(k*2*y + (0.9 if lado else 0)) + 8*math.sin(k*4*y + 0.4)
    # trilha (neve batida) com borda suave
    mask = Image.new("L", (W, H*3), 0); md = ImageDraw.Draw(mask)
    for k in range(3):
        e = [(borda(y, 0), y + k*H) for y in range(-20, H+21, 5)]
        r = [(borda(y, 1), y + k*H) for y in range(H+20, -21, -5)]
        md.polygon(e + r, fill=255)
    mask = mask.filter(ImageFilter.GaussianBlur(3)).crop((0, H, W, H*2))
    trilha = Image.new("RGB", (W, H), (222, 230, 244))
    td = ImageDraw.Draw(trilha)
    for _ in range(170):
        y = random.randint(0, H-1)
        blot(td, random.randint(int(borda(y, 0)), int(borda(y, 1))), y, random.randint(14, 40), random.randint(5, 14),
             random.choice([(214, 224, 241), (230, 237, 248), (208, 219, 238)]))
    trilha = trilha.filter(ImageFilter.GaussianBlur(2))
    td = ImageDraw.Draw(trilha)
    # sombra suave da beirada (volume da neve acumulada)
    sombra = Image.new("L", (W, H*3), 0); sd = ImageDraw.Draw(sombra)
    for k in range(3):
        for lado in (0, 1):
            xs = [(borda(y, lado) + (-14 if lado == 0 else 14), y + k*H) for y in range(-20, H+21, 5)]
            sd.line(xs, fill=150, width=26)
    sombra = sombra.filter(ImageFilter.GaussianBlur(8)).crop((0, H, W, H*2))
    azul = Image.new("RGB", (W, H), (170, 190, 220))
    im = Image.composite(azul, im, sombra.point(lambda v: int(v*0.55)))
    im = Image.composite(trilha, im, mask)
    d = ImageDraw.Draw(im)
    # sulcos de trenó/pegadas: duas linhas onduladas
    for off in (-34, 34):
        for kk in (-H, 0, H):
            pts = [(480 + off + 18*math.sin(2*math.pi*y/H*2 + 0.5) , y + kk) for y in range(-20, H+21, 4)]
            d.line(pts, fill=(190, 204, 226), width=6); d.line([(x+2, y) for x, y in pts], fill=(236, 242, 252), width=2)
    # pegadas de patinhas (duplas alternadas)
    for i, y in enumerate(range(20, H, 46)):
        x = 480 + 18*math.sin(2*math.pi*y/H*2 + 0.5) + (-9 if i % 2 else 9)
        blot(d, x, y, 4, 3, (176, 192, 218)); blot(d, x-5, y-5, 1.5, 1.5, (176, 192, 218)); blot(d, x+5, y-5, 1.5, 1.5, (176, 192, 218))
    # cascalho / terra aparecendo
    for _ in range(40):
        y = random.randint(0, H-1); x = random.randint(int(borda(y, 0))+14, int(borda(y, 1))-14)
        blot(d, x, y, random.randint(2, 5), random.randint(2, 3), random.choice([(120, 126, 140), (142, 132, 120)]))
    # brilhos de gelo (estrelinhas)
    for _ in range(190):
        x = random.randint(0, W-1); y = random.randint(0, H-1)
        def estrela(yy, x=x):
            d.line([x-3, yy, x+3, yy], fill=(255, 255, 255), width=1); d.line([x, yy-3, x, yy+3], fill=(255, 255, 255), width=1)
        wrap_draw(estrela, y, 4)
    return im

def rocha_gelo(nome, w=140, h=120, escala_neve=1.0):
    im = novo(w, h); d = ImageDraw.Draw(im)
    sombra_elipse(d, w/2+8, h-10, w*0.43, 11, 85)
    pts = [(0.06, 0.82), (0.02, 0.48), (0.22, 0.14), (0.52, 0.03), (0.86, 0.16), (0.99, 0.5), (0.92, 0.82), (0.55, 0.93)]
    P = [(x*w*S, y*(h-10)*S) for x, y in pts]
    d.polygon(P, fill=(118, 128, 150))
    d.polygon([P[1], P[2], P[3], (w*0.5*S, h*0.5*S)], fill=(150, 162, 184))
    d.polygon([P[4], P[5], P[6], (w*0.52*S, h*0.55*S)], fill=(86, 96, 120))
    d.polygon([P[2], P[3], P[4], (w*0.74*S, h*0.34*S), (w*0.5*S, h*0.4*S), (w*0.3*S, h*0.34*S)], fill=(248, 251, 255))  # neve no topo
    d.polygon([P[3], P[4], (w*0.74*S, h*0.34*S), (w*0.52*S, h*0.2*S)], fill=(208, 224, 244))
    d.line([(w*0.38*S, h*0.5*S), (w*0.46*S, h*0.74*S)], fill=(92, 104, 130), width=2*S)
    d.line(P + [P[0]], fill=(76, 86, 108), width=2*S)
    # reflexo de gelo
    d.polygon([(w*0.62*S, h*0.55*S), (w*0.74*S, h*0.5*S), (w*0.7*S, h*0.7*S)], fill=(150, 196, 232))
    fim(im, w, h).save(nome)

def monte_neve(nome, w=96, h=54):
    im = novo(w, h); d = ImageDraw.Draw(im)
    sombra_elipse(d, w/2+4, h-8, w*0.42, 7, 55)
    d.ellipse([2*S, 10*S, (w-2)*S, (h-6)*S], fill=(214, 228, 246))
    d.ellipse([6*S, 6*S, (w*0.78)*S, (h-12)*S], fill=(250, 252, 255))
    d.ellipse([(w*0.45)*S, 14*S, (w-8)*S, (h-12)*S], fill=(226, 238, 252))
    fim(im, w, h).save(nome)

def chale(nome, w=130, h=130):
    im = novo(w, h); d = ImageDraw.Draw(im)
    sombra_elipse(d, w/2+10, h-12, w*0.42, 12, 90)
    d.rectangle([10*S, 30*S, (w-10)*S, (h-18)*S], fill=(116, 74, 40))          # parede lateral
    d.rectangle([10*S, (h-34)*S, (w-10)*S, (h-18)*S], fill=(92, 58, 32))
    for x in range(14, w-10, 12):
        d.line([x*S, (h-34)*S, x*S, (h-18)*S], fill=(70, 44, 24), width=S)
    d.rectangle([(w/2-9)*S, (h-36)*S, (w/2+9)*S, (h-18)*S], fill=(60, 38, 22))   # porta
    d.rectangle([(w/2+22)*S, (h-32)*S, (w/2+38)*S, (h-22)*S], fill=(250, 214, 120))  # janela acesa
    d.polygon([(4*S, 36*S), (w/2*S, 2*S), ((w-4)*S, 36*S), ((w-4)*S, (h-36)*S), (4*S, (h-36)*S)], fill=(150, 92, 56))  # telhado
    d.polygon([(w/2*S, 2*S), ((w-4)*S, 36*S), ((w-4)*S, (h-36)*S), (w/2*S, (h-60)*S)], fill=(126, 76, 46))
    d.polygon([(8*S, 38*S), (w/2*S, 6*S), ((w-8)*S, 38*S), ((w-12)*S, 52*S), (w/2*S, 30*S), (12*S, 52*S)], fill=(248, 251, 255))  # neve no telhado
    d.polygon([(w/2*S, 6*S), ((w-8)*S, 38*S), ((w-12)*S, 52*S), (w/2*S, 30*S)], fill=(214, 228, 246))
    d.rectangle([(w-40)*S, 4*S, (w-28)*S, 26*S], fill=(110, 80, 70)); d.rectangle([(w-42)*S, 2*S, (w-26)*S, 8*S], fill=(246, 250, 255))  # chamine
    fim(im, w, h).save(nome)

def boneco_neve(nome, w=52, h=80):
    im = novo(w, h); d = ImageDraw.Draw(im)
    sombra_elipse(d, w/2+5, h-6, 18, 5, 70)
    for cy, r, cor in ((h-24, 19, (244, 248, 255)), (h-48, 14, (250, 252, 255)), (h-64, 10, (252, 254, 255))):
        d.ellipse([(w/2-r)*S, (cy-r)*S, (w/2+r)*S, (cy+r)*S], fill=cor)
        d.arc([(w/2-r)*S, (cy-r)*S, (w/2+r)*S, (cy+r)*S], 20, 150, fill=(198, 214, 236), width=2*S)
    for dx in (-3.5, 3.5):
        d.ellipse([(w/2+dx-1.2)*S, (h-67)*S, (w/2+dx+1.2)*S, (h-64.6)*S], fill=(30, 30, 40))
    d.polygon([((w/2)*S, (h-64)*S), ((w/2+9)*S, (h-62)*S), ((w/2)*S, (h-61)*S)], fill=(238, 120, 30))
    d.rectangle([(w/2-10)*S, (h-58)*S, (w/2+10)*S, (h-54)*S], fill=(200, 40, 50))   # cachecol
    fim(im, w, h).save(nome)

# ================================================================ EFEITOS VIVOS
def sombra_nuvem(nome, w=300, h=170, seed=1):
    r = random.Random(seed)
    m = Image.new("L", (w, h), 0); d = ImageDraw.Draw(m)
    for _ in range(9):
        rx, ry = r.randint(40, 80), r.randint(26, 52); cx, cy = r.randint(70, w-70), r.randint(45, h-45)
        d.ellipse([cx-rx, cy-ry, cx+rx, cy+ry], fill=255)
    m = m.filter(ImageFilter.GaussianBlur(16)).point(lambda v: int(v*0.30))
    im = Image.new("RGBA", (w, h), (10, 20, 40, 0)); im.putalpha(m); im.save(nome)

def passaro(nome, aberto, w=46, h=38):
    im = Image.new("RGBA", (w*S, h*S), (0, 0, 0, 0)); d = ImageDraw.Draw(im)
    cx = w/2*S; cy = h/2*S; c = (235, 235, 240, 255); esc = (190, 190, 200, 255)
    span = 21 if aberto else 13; dy = 4 if aberto else -5
    for lado in (-1, 1):
        d.polygon([(cx+lado*3*S, cy-3*S), (cx+lado*span*S, cy+dy*S-4*S), (cx+lado*(span-3)*S, cy+dy*S+6*S), (cx+lado*3*S, cy+5*S)], fill=c)
        d.line([(cx+lado*3*S, cy), (cx+lado*(span-2)*S, cy+dy*S)], fill=esc, width=S)
    d.ellipse([cx-5*S, cy-9*S, cx+5*S, cy+9*S], fill=c)
    d.polygon([(cx-3*S, cy+7*S), (cx+3*S, cy+7*S), (cx, cy+16*S)], fill=esc)
    d.ellipse([cx-3*S, cy-12*S, cx+3*S, cy-6*S], fill=c)
    d.polygon([(cx-1.5*S, cy-12*S), (cx+1.5*S, cy-12*S), (cx, cy-15*S)], fill=(250, 190, 90, 255))
    im.resize((w, h), Image.LANCZOS).save(nome)

def folha(nome, w=18, h=12):
    im = Image.new("RGBA", (w*S, h*S), (0, 0, 0, 0)); d = ImageDraw.Draw(im)
    d.ellipse([1*S, 1*S, (w-1)*S, (h-1)*S], fill=(240, 240, 240, 255))
    d.line([(2*S, h/2*S), ((w-2)*S, h/2*S)], fill=(180, 180, 180, 255), width=S)
    im.resize((w, h), Image.LANCZOS).save(nome)

def ponto_macio(nome, w, miolo, blur, alfa=255):
    m = Image.new("L", (w*S, w*S), 0); d = ImageDraw.Draw(m)
    d.ellipse([(w/2-miolo)*S, (w/2-miolo)*S, (w/2+miolo)*S, (w/2+miolo)*S], fill=alfa)
    m = m.filter(ImageFilter.GaussianBlur(blur*S)).resize((w, w), Image.LANCZOS)
    im = Image.new("RGBA", (w, w), (255, 255, 255, 0)); im.putalpha(m); im.save(nome)

def raio_luz(nome, w=150, h=640):
    m = Image.new("L", (w*2, h), 0); d = ImageDraw.Draw(m)
    d.polygon([(w*0.9, 0), (w*1.35, 0), (w*0.55, h), (w*0.08, h)], fill=255)
    m = m.filter(ImageFilter.GaussianBlur(20))
    # esmaece no comprimento (mais forte no topo)
    grad = Image.linear_gradient("L").resize((w*2, h)).transpose(Image.FLIP_TOP_BOTTOM)
    m = ImageChops.multiply(m, grad.point(lambda v: int(v*0.9 + 20)))
    im = Image.new("RGBA", (w*2, h), (255, 244, 190, 0)); im.putalpha(m); im.save(nome)

if __name__ == "__main__":
    # cidade
    tile_cidade().save(F+"rua_cidade.png")
    arvore(D+"cidade_arvore.png"); poste(D+"cidade_poste.png"); hidrante(D+"cidade_hidrante.png")
    banco(D+"cidade_banco.png"); vaso(D+"cidade_vaso.png")
    # floresta
    tile_floresta().save(F+"trilha_floresta.png")
    arvore_grande(D+"floresta_arvore_grande.png")
    arvore(D+"floresta_arvore.png", 130, 140, (44, 128, 62), (104, 186, 74), (24, 82, 46), n=16)
    pinheiro(D+"floresta_pinheiro.png"); samambaia(D+"floresta_samambaia.png"); cogumelos(D+"floresta_cogumelos.png")
    tronco_caido(D+"floresta_tronco_caido.png"); pedra_musgo(D+"floresta_pedra_musgo.png"); flores(D+"floresta_flores.png")
    arbusto(D+"floresta_arbusto.png", base=(60, 148, 56), claro=(130, 204, 76), escuro=(34, 96, 46)); toco(D+"floresta_toco.png")
    # alpes
    tile_alpes().save(F+"trilha_alpes.png")
    pinheiro(D+"alpes_pinheiro_a.png", 84, 140, ((26, 86, 62), (30, 100, 68), (38, 116, 76)), neve=True)
    pinheiro(D+"alpes_pinheiro_b.png", 64, 108, ((24, 80, 58), (30, 96, 66), (36, 110, 74)), neve=True)
    rocha_gelo(D+"alpes_rocha.png"); rocha_gelo(D+"alpes_rocha_pequena.png", 76, 66)
    monte_neve(D+"alpes_monte_neve.png"); chale(D+"alpes_chale.png"); boneco_neve(D+"alpes_boneco_neve.png")
    # efeitos
    sombra_nuvem(V+"sombra_nuvem_a.png", seed=1); sombra_nuvem(V+"sombra_nuvem_b.png", 360, 190, seed=4)
    passaro(V+"passaro_1.png", True); passaro(V+"passaro_2.png", False); folha(V+"folha.png")
    ponto_macio(V+"neve.png", 14, 3.2, 1.6); ponto_macio(V+"vaga_lume.png", 28, 3, 6)
    raio_luz(V+"raio_luz.png")
    print("ok")
