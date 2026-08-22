import os, re

path = 'src/main/resources/modules/market/config.yml'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

deal_config = '''
deal-settings:
  chunk-size: 100
  chunk-rewards:
    - "eco give %player% 500"
    - "msg %player% &a&l+ $500 &7(Th\u01b0\u1edfng \u0111\u00f3ng g\u00f3p Th\u01b0\u01a1ng V\u1ee5)"
  top1-rewards:
    - "eco give %player% 500000"
    - "broadcast &b\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584"
    - "broadcast &6&l\u272a TH\u01af\u01a0NG V\u1ee4 B\u1ea0C T\u1ef6 K\u1ebeT TH\u00daC \u272a"
    - "broadcast &e\u0110\u1ea1i gia &a%player% &e\u0111\u00e3 xu\u1ea5t s\u1eafc gi\u00e0nh TOP 1"
    - "broadcast &ev\u00e0 nh\u1eadn \u0111\u01b0\u1ee3c &a$500,000&e t\u1eeb T\u1eadp \u0111o\u00e0n!"
    - "broadcast &b\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584"
  top2-rewards:
    - "eco give %player% 250000"
    - "msg %player% &eB\u1ea1n \u0111\u1ea1t TOP 2 Th\u01b0\u01a1ng V\u1ee5 v\u00e0 nh\u1eadn &a$250,000"
  top3-rewards:
    - "eco give %player% 100000"
    - "msg %player% &eB\u1ea1n \u0111\u1ea1t TOP 3 Th\u01b0\u01a1ng V\u1ee5 v\u00e0 nh\u1eadn &a$100,000"
  possible-deals:
    - material: WHEAT
      target: 50000
    - material: OAK_LOG
      target: 25000
    - material: COBBLESTONE
      target: 100000
    - material: IRON_INGOT
      target: 15000
    - material: CARROT
      target: 50000
    - material: STONE
      target: 50000
'''

if 'deal-settings:' not in text:
    text += deal_config
    with open(path, 'w', encoding='utf-8') as f:
        f.write(text)
