# Rustling Spots - Custom Spot Definitions

Une presentation complete du systeme de custom rustling spots via datapack pour `Rustling Spots 1.21.1`.

---

## Etat du datapack d'exemple

Oui, le datapack d'exemple est pret et complet pour le format actuellement supporte.

Le dossier `example_datapacks/custom_swamp_spot_pack` contient deja :

- un `pack.mcmeta` valide pour `1.21.1`
- un custom spot complet
- une famille Pokemon custom
- une famille de loot custom
- un `README.txt` d'installation
- un exemple de particules mixees
- un exemple directement testable avec commande

En clair : c'est une bonne base a dupliquer pour creer ses propres spots.

> Important :
> le datapack d'exemple est complet pour le systeme actuel, mais il ne montre pas absolument toutes les options avances possibles. Par exemple, il ne montre pas `shiny`, `shinyChance` ou `families` dans les entrees Pokemon. La page ci-dessous couvre aussi ces cas.

---

## A quoi sert ce systeme

Le systeme permet de creer des rustling spots personnalises via des fichiers JSON, sans addon code, sans ressources speciales, sans modele 3D et sans API externe.

Vous pouvez personnaliser :

- ou le spot peut apparaitre
- dans quelles dimensions
- dans quels biomes
- sur quels blocs
- quelle famille Pokemon il utilise
- quelle famille de loot il utilise
- quel nom il affiche
- quelles particules il joue
- sa priorite et son poids de selection

Ce qui reste volontairement partage avec le mod :

- le comportement general du rustling spot
- le cycle de vie du spot
- la logique d'interaction
- la base visuelle choisie via `visual_family`

## Famille visuelle

Chaque custom spot peut choisir une famille visuelle avec le champ optionnel `visual_family`.

Ce champ controle le rendu de base et les particules de fallback :

- valeurs supportees : `grass`, `sand`, `water`, `snow`, `leaves`, `cave`, `netherflamme`, `soulflame`, `flying`
- si le champ est absent ou invalide, le spot garde le fallback historique `grass`
- les particules personnalisees restent la meilleure facon de donner une identite forte au spot

Le format datapack ne cree pas un nouveau systeme de rendu complet, mais il permet maintenant d'utiliser la famille visuelle la plus proche du theme du spot.

---

## Installation du datapack d'exemple

Placez le dossier ici :

```text
world/datapacks/custom_swamp_spot_pack
```

Puis en jeu :

```mcfunction
/reload
/rustlingspots spawn rustlingspots:swamp_custom
```

Le `pack.mcmeta` inclus est :

```json
{
  "pack": {
    "description": "Rustling Spots example datapack: custom swamp spot",
    "pack_format": 48,
    "supported_formats": {
      "min_inclusive": 48,
      "max_inclusive": 48
    }
  }
}
```

---

## Structure d'un datapack custom

Le systeme repose sur 3 groupes de fichiers :

```text
<votre_datapack>/
|- pack.mcmeta
`- data/
   `- <namespace>/
      `- rustling_spots/
         |- spot_definitions/
         |- pokemon_families/
         `- loot_families/
```

### Dossiers reconnus

- `data/<namespace>/rustling_spots/spot_definitions/*.json`
  Role : definition des custom spots eux-memes.

- `data/<namespace>/rustling_spots/pokemon_families/*.json`
  Role : pools Pokemon reutilisables par plusieurs spots.

- `data/<namespace>/rustling_spots/loot_families/*.json`
  Role : pools de loot reutilisables par plusieurs spots.

### Namespace

Le `namespace` est la premiere partie d'un identifiant.

Exemples :

- `rustlingspots:swamp_custom`
- `mymodpack:haunted_forest`
- `myserver:red_canyon`

Dans `mymodpack:haunted_forest` :

- namespace = `mymodpack`
- path = `haunted_forest`

Le datapack d'exemple utilise `rustlingspots`, mais vous pouvez utiliser votre propre namespace.

---

## Fichiers inclus dans l'exemple

Le datapack d'exemple fourni contient exactement ceci :

```text
example_datapacks/custom_swamp_spot_pack/
|- pack.mcmeta
|- README.txt
`- data/
   `- rustlingspots/
      `- rustling_spots/
         |- spot_definitions/
         |  `- swamp.json
         |- pokemon_families/
         |  `- swamp.json
         `- loot_families/
            `- swamp.json
```

Son identifiant de spot est :

```text
rustlingspots:swamp_custom
```

---

## Comment le jeu choisit un spot

Quand le mod tente de faire apparaitre un rustling spot :

1. il detecte d'abord le spot vanilla/built-in possible selon le bloc
2. il ajoute ensuite tous les custom spots de datapack qui correspondent a la position
3. il garde uniquement les candidats avec la plus haute `priority`
4. il choisit ensuite un candidat parmi ceux-la selon leur `weight`

## Consequence pratique

- un custom spot avec `priority: 10` passera devant un built-in normal
- un custom spot avec `priority: 0` peut coexister avec les built-ins
- `weight` ne sert qu'entre spots de meme priorite

## Priorite des built-ins

Les spots built-in du mod utilisent :

- `priority = 0`
- `weight = 1`

Donc si vous voulez qu'un custom spot remplace naturellement le comportement normal dans sa zone, utilisez en general une priorite superieure a `0`.

---

## Le fichier `spot_definitions`

Chaque custom spot est defini dans un fichier JSON dans :

```text
data/<namespace>/rustling_spots/spot_definitions/<nom>.json
```

### Exemple complet

```json
{
  "format_version": 1,
  "id": "rustlingspots:swamp_custom",
  "display_name": "Swamp Spot",
  "priority": 10,
  "weight": 5,
  "visual_family": "leaves",
  "dimensions": [
    "minecraft:overworld"
  ],
  "biomes": [
    "minecraft:swamp",
    "minecraft:mangrove_swamp",
    "#minecraft:is_forest"
  ],
  "blocks": [
    "minecraft:mud",
    "minecraft:grass_block",
    "#minecraft:dirt"
  ],
  "pokemon_family": "rustlingspots:swamp",
  "loot_family": "rustlingspots:swamp",
  "particles": [
    {
      "type": "rustlingspots:grass_burst",
      "weight": 7
    },
    {
      "type": "minecraft:happy_villager",
      "weight": 3
    }
  ]
}
```

### Champs supportes

| Champ | Obligatoire | Description |
| --- | --- | --- |
| `format_version` | Oui | Version du format. Actuellement, seule la valeur `1` est acceptee. |
| `id` | Oui | Identifiant complet du custom spot. |
| `display_name` | Non | Nom lisible affiche dans les messages et commandes de debug. |
| `visual_family` | Non | Famille visuelle utilisee pour le rendu et les particules de fallback. Si absent ou invalide, le spot garde le fallback historique `grass`. |
| `priority` | Oui | Priorite utilisee quand plusieurs spots matchent au meme endroit. |
| `weight` | Oui | Poids de selection entre spots de meme priorite. Doit etre > `0`. |
| `dimensions` | Non | Liste de dimensions autorisees. Si absent ou invalide, le spot est limite a l'Overworld. |
| `biomes` | Oui | Liste de biomes ou tags de biomes autorises. |
| `blocks` | Oui | Liste de blocs ou tags de blocs autorises sous le spot. |
| `pokemon_family` | Oui | Famille Pokemon utilisee par le spot. |
| `loot_family` | Oui | Famille de loot utilisee par le spot. |
| `particles` | Non | Liste de particules ponderees. Si absent ou vide, fallback sur les particules grass par defaut. |

### Valeurs accepteees pour `biomes` et `blocks`

Vous pouvez melanger :

- des IDs directs
- des tags prefixed avec `#`

Exemples valides :

```json
"biomes": [
  "minecraft:swamp",
  "#minecraft:is_forest"
]
```

```json
"blocks": [
  "minecraft:mud",
  "#minecraft:dirt"
]
```

### Regles exactes du fichier spot

- `format_version` doit etre `1`
- `weight` doit etre strictement positif
- `biomes` ne peut pas etre vide
- `blocks` ne peut pas etre vide
- `pokemon_family` doit exister, sinon la definition est ignoree
- `loot_family` peut etre inconnue, mais dans ce cas le spot retombe sur le global loot
- `display_name` est optionnel
- `visual_family` est optionnel et accepte `grass`, `sand`, `water`, `snow`, `leaves`, `cave`, `netherflamme`, `soulflame` ou `flying`
- `dimensions` est optionnel
- `particles` est optionnel

### Fallbacks importants

- `dimensions` absent -> Overworld only
- `visual_family` absent ou invalide -> famille visuelle grass
- `particles` absent -> particules grass par defaut
- `particles` presentes mais toutes invalides -> particules grass par defaut
- `loot_family` inconnue -> global loot
- `pokemon_family` inconnue -> le spot custom n'est pas charge

### Attention aux IDs reserves

Les IDs internes du mod ne peuvent pas etre ecrases par datapack.

Ces IDs sont reserves :

```text
rustlingspots:grass
rustlingspots:sand
rustlingspots:water
rustlingspots:snow
rustlingspots:leaves
rustlingspots:cave
rustlingspots:netherflamme
rustlingspots:soulflame
rustlingspots:flying
```

Si vous utilisez un de ces IDs dans un custom spot, il sera refuse.

---

## Le fichier `pokemon_families`

Les Pokemon ne sont pas definis directement dans le spot.
Le spot pointe vers une famille Pokemon.

Emplacement :

```text
data/<namespace>/rustling_spots/pokemon_families/<nom>.json
```

### Exemple du datapack d'exemple

```json
[
  { "species": "wooper", "weight": 10, "min_level": 1, "max_level": 18 },
  { "species": "paldean-wooper", "weight": 10, "min_level": 1, "max_level": 18 },
  { "species": "lotad", "weight": 9, "min_level": 1, "max_level": 20 },
  { "species": "surskit", "weight": 9, "min_level": 1, "max_level": 20 },
  { "species": "croagunk", "weight": 8, "min_level": 8, "max_level": 24 },
  { "species": "stunfisk", "weight": 6, "min_level": 12, "max_level": 28 },
  { "species": "marshtomp", "weight": 3, "min_level": 18, "max_level": 36 },
  { "species": "quagsire", "weight": 3, "min_level": 18, "max_level": 36 },
  { "species": "swampert", "weight": 1, "min_level": 36, "max_level": 55 }
]
```

### Champs supportes par entree Pokemon

| Champ | Obligatoire | Description |
| --- | --- | --- |
| `species` | Oui | Espece Cobblemon a invoquer. |
| `weight` | Oui | Poids de selection. Doit etre > `0`. |
| `min_level` | Non | Niveau minimum possible. |
| `max_level` | Non | Niveau maximum possible. |
| `shiny` | Non | Si present, force ou interdit l'etat shiny pour cette entree. |
| `shinyChance` | Non | Chance shiny specifique a cette entree. Attention : le champ supporte est `shinyChance` en camelCase. |
| `families` | Non | Liste avancee de familles auxquelles cette entree appartient. |

### Point important sur `shinyChance`

Le champ reconnu dans le JSON est :

```json
"shinyChance": 0.15
```

Le champ `shiny_chance` n'est pas lu pour les entrees de pools Pokemon. Utilisez toujours `shinyChance`.

### Exemple avance

```json
[
  {
    "species": "gastly",
    "weight": 8,
    "min_level": 12,
    "max_level": 28,
    "shinyChance": 0.10
  },
  {
    "species": "mimikyu",
    "weight": 1,
    "min_level": 35,
    "max_level": 55,
    "shiny": true
  }
]
```

### A quoi sert `families`

Dans un fichier de famille datapack, vous n'avez generalement pas besoin de le renseigner.

Si vous ne mettez pas `families`, le mod considere automatiquement que l'entree appartient a la famille representee par le fichier.

Exemple :

```text
data/rustlingspots/rustling_spots/pokemon_families/swamp.json
```

donne automatiquement la famille :

```text
rustlingspots:swamp
```

### Lien depuis le spot

Si votre fichier est :

```text
data/rustlingspots/rustling_spots/pokemon_families/swamp.json
```

alors le spot doit pointer vers :

```json
"pokemon_family": "rustlingspots:swamp"
```

### Regles utiles

- les entrees avec `weight <= 0` sont ignorees
- les especes vides sont ignorees
- les niveaux sont bornes par la config du mod au moment du spawn
- si aucune entree valide n'existe pour la famille, aucun Pokemon n'est invoque

---

## Le fichier `loot_families`

Le loot suit la meme logique : le spot reference une famille de loot.

Emplacement :

```text
data/<namespace>/rustling_spots/loot_families/<nom>.json
```

### Exemple du datapack d'exemple

```json
[
  { "item": "minecraft:slime_ball", "min": 1, "max": 3, "weight": 5 },
  { "item": "minecraft:vine", "min": 1, "max": 3, "weight": 4 },
  { "item": "minecraft:lily_pad", "min": 1, "max": 2, "weight": 3 },
  { "item": "minecraft:mud", "min": 2, "max": 5, "weight": 4 },
  { "item": "minecraft:mangrove_propagule", "min": 1, "max": 2, "weight": 2 },
  { "item": "cobblemon:pecha_berry", "min": 1, "max": 2, "weight": 3 },
  { "item": "cobblemon:net_ball", "min": 1, "max": 1, "weight": 2 },
  { "item": "cobblemon:great_ball", "min": 1, "max": 1, "weight": 2 }
]
```

### Champs supportes par entree loot

| Champ | Obligatoire | Description |
| --- | --- | --- |
| `item` | Oui | ID complet de l'objet. |
| `min` | Non | Quantite minimum. Defaut `1`. |
| `max` | Non | Quantite maximum. Defaut `1`. |
| `weight` | Oui | Poids de selection. Doit etre > `0`. |

### Regles utiles

- `item` doit exister dans les registres du jeu
- `min` est automatiquement remonte a au moins `1`
- `max` est automatiquement remonte a au moins `min`
- les entrees invalides sont ignorees

### Fusion avec le global loot

Point tres important : une famille de loot datapack ne remplace pas seulement son contenu local.

Le mod construit le pool final ainsi :

1. il part du `global_loot`
2. il ajoute les entrees de votre famille
3. il fusionne les doublons identiques en cumulant leurs poids

Donc une `loot_family` datapack ajoute naturellement du contexte par-dessus le loot global.

### Lien depuis le spot

Si votre fichier est :

```text
data/rustlingspots/rustling_spots/loot_families/swamp.json
```

alors le spot doit pointer vers :

```json
"loot_family": "rustlingspots:swamp"
```

### Si la famille de loot est inconnue

Le spot n'est pas supprime.
Il reste charge, mais utilise le global loot a la place.

---

## Particules supportees

Le champ `particles` accepte une liste d'objets :

```json
"particles": [
  { "type": "rustlingspots:grass_burst", "weight": 7 },
  { "type": "minecraft:happy_villager", "weight": 3 }
]
```

### Format supporte

Chaque entree accepte seulement :

- `type`
- `weight`

Il n'y a pas actuellement de support pour :

- vitesse custom
- offset custom
- nombre de particules par entree
- couleur custom
- parametres avances par particule

### Regle technique

Le type doit etre une particule simple reconnue par le jeu.
Si elle est inconnue ou non supportee, elle est ignoree.

### Particules Rustling Spots utiles

Vous pouvez deja reutiliser celles du mod :

- `rustlingspots:grass_burst`
- `rustlingspots:sand_burst`
- `rustlingspots:water_burst`
- `rustlingspots:snow_burst`
- `rustlingspots:leaves_burst`
- `rustlingspots:cave_burst`
- `rustlingspots:netherflamme_burst`
- `rustlingspots:soulflame_burst`
- `rustlingspots:flying_burst`
- `rustlingspots:shiny_sparkle_one`
- `rustlingspots:shiny_sparkle_two`

Vous pouvez aussi melanger avec des particules vanilla simples comme :

- `minecraft:happy_villager`
- `minecraft:poof`
- `minecraft:cloud`

### Fallback particules

Si `particles` est absent, vide, ou ne contient aucune entree valide :

- le spot reste charge
- il retombe sur les particules grass par defaut

---

## Commandes utiles

### Recharger

```mcfunction
/reload
```

Recharge les datapacks du monde, donc aussi les custom spots, familles Pokemon et familles loot.

```mcfunction
/rustlingspots reload
```

Recharge les ressources Rustling Spots et relance aussi le rechargement des donnees datapack du mod.

### Tester un custom spot

```mcfunction
/rustlingspots spawn rustlingspots:swamp_custom
```

### Tester une version shiny

```mcfunction
/rustlingspots spawnshiny rustlingspots:swamp_custom
```

### Scanner les spots actifs autour du joueur

```mcfunction
/rustlingspots scan 64
```

ou avec filtre de famille built-in :

```mcfunction
/rustlingspots scan 64 grass
```

---

## Comment creer son propre spot

Voici la methode la plus simple et la plus sure.

### 1. Dupliquez l'exemple

Copiez :

```text
example_datapacks/custom_swamp_spot_pack
```

vers un nouveau nom, par exemple :

```text
my_biome_spot_pack
```

### 2. Modifiez `pack.mcmeta`

Changez simplement la description si vous voulez.

### 3. Creez votre nouvel ID

Dans `spot_definitions/swamp.json`, changez :

```json
"id": "rustlingspots:swamp_custom"
```

par quelque chose d'unique, par exemple :

```json
"id": "mymodpack:haunted_forest"
```

Ajoutez aussi un nom lisible :

```json
"display_name": "Haunted Forest Spot"
```

### 4. Adaptez la zone d'apparition

Modifiez :

- `dimensions`
- `biomes`
- `blocks`

Exemple :

```json
"dimensions": [
  "minecraft:overworld"
],
"biomes": [
  "minecraft:dark_forest",
  "#minecraft:is_forest"
],
"blocks": [
  "minecraft:grass_block",
  "minecraft:podzol",
  "#minecraft:dirt"
]
```

### 5. Choisissez le comportement de priorite

Si vous voulez que votre custom spot gagne contre les built-ins :

```json
"priority": 10
```

Si vous voulez qu'il partage ses chances avec eux :

```json
"priority": 0
```

Puis reglez son poids relatif :

```json
"weight": 5
```

### 6. Creez votre famille Pokemon

Editez le fichier dans :

```text
data/<namespace>/rustling_spots/pokemon_families/<nom>.json
```

Puis reliez-le dans le spot :

```json
"pokemon_family": "<namespace>:<nom>"
```

### 7. Creez votre famille de loot

Editez le fichier dans :

```text
data/<namespace>/rustling_spots/loot_families/<nom>.json
```

Puis reliez-le dans le spot :

```json
"loot_family": "<namespace>:<nom>"
```

### 8. Choisissez vos particules

Exemple simple :

```json
"particles": [
  { "type": "rustlingspots:leaves_burst", "weight": 8 },
  { "type": "minecraft:happy_villager", "weight": 2 }
]
```

### 9. Testez en jeu

Installez le datapack, puis :

```mcfunction
/reload
/rustlingspots spawn mymodpack:haunted_forest
```

### 10. Verifiez le spawn naturel

Confirmez ensuite que le spot apparait bien dans les bons biomes, sur les bons blocs et avec le bon comportement de priorite.

---

## Exemple de variante complete

### Spot

```json
{
  "format_version": 1,
  "id": "mymodpack:haunted_forest",
  "display_name": "Haunted Forest Spot",
  "priority": 12,
  "weight": 6,
  "dimensions": [
    "minecraft:overworld"
  ],
  "biomes": [
    "minecraft:dark_forest",
    "#minecraft:is_forest"
  ],
  "blocks": [
    "minecraft:grass_block",
    "minecraft:podzol",
    "#minecraft:dirt"
  ],
  "pokemon_family": "mymodpack:haunted_forest",
  "loot_family": "mymodpack:haunted_forest",
  "particles": [
    { "type": "rustlingspots:leaves_burst", "weight": 6 },
    { "type": "rustlingspots:shiny_sparkle_one", "weight": 1 },
    { "type": "minecraft:happy_villager", "weight": 2 }
  ]
}
```

### Pokemon

```json
[
  { "species": "murkrow", "weight": 8, "min_level": 10, "max_level": 24 },
  { "species": "phantump", "weight": 7, "min_level": 12, "max_level": 28 },
  { "species": "shuppet", "weight": 7, "min_level": 12, "max_level": 28 },
  { "species": "mimikyu", "weight": 1, "min_level": 30, "max_level": 45, "shinyChance": 0.05 }
]
```

### Loot

```json
[
  { "item": "minecraft:bone", "min": 1, "max": 3, "weight": 4 },
  { "item": "minecraft:spider_eye", "min": 1, "max": 2, "weight": 3 },
  { "item": "minecraft:string", "min": 1, "max": 4, "weight": 4 },
  { "item": "cobblemon:spell_tag", "min": 1, "max": 1, "weight": 1 }
]
```

---

## Validation, erreurs et comportement en cas de probleme

Le systeme a ete pense pour etre tolerant aux erreurs.
Le but est d'eviter qu'un mauvais JSON fasse planter le serveur ou le monde.

### Ce qui se passe si...

- `format_version` n'est pas supporte
  Le spot n'est pas charge.

- `id` est invalide
  Le spot n'est pas charge.

- `pokemon_family` est inconnue
  Le spot n'est pas charge.

- `loot_family` est inconnue
  Le spot reste charge, mais utilise le global loot.

- `particles` contient des particules invalides
  Les entrees invalides sont ignorees.

- aucune particule valide n'est conservee
  Fallback sur les particules grass.

- `biomes` ou `blocks` deviennent vides apres validation
  Le spot n'est pas charge.

- deux fichiers definissent le meme `id`
  la derniere definition chargee gagne

### Conseil pratique

Quand un spot ne marche pas :

1. testez son ID avec `/rustlingspots spawn`
2. relancez `/reload`
3. verifiez l'orthographe du namespace
4. verifiez l'existence de `pokemon_family`
5. verifiez que les tableaux `biomes` et `blocks` ne sont pas vides

---

## Relation avec les fichiers de config du mod

Le datapack n'est pas le seul endroit ou des familles peuvent exister.

Le mod gere aussi des fichiers de config sur disque, notamment :

```text
config/rustlingspots/pokemon/families/
config/rustlingspots/loot/families/
config/rustlingspots/loot/global_loot.json
```

### Ce que ca change

- `pokemon_family` peut pointer vers une famille definie dans la config ou dans un datapack
- `loot_family` peut pointer vers une famille definie dans la config ou dans un datapack
- les familles de loot se construisent toujours avec le `global_loot` comme base

Pour une presentation CurseForge orientee utilisateur, vous pouvez simplement expliquer que :

- le datapack ajoute ses propres familles
- le mod peut aussi utiliser ses fichiers de config normaux

---

## Checklist avant publication

- le datapack est bien dans `world/datapacks/`
- le `pack.mcmeta` est valide
- `/reload` fonctionne sans erreur
- `/rustlingspots spawn <id>` fonctionne
- `display_name` s'affiche bien
- les bons Pokemon apparaissent
- le bon loot tombe
- le spot apparait dans les bons biomes
- le spot apparait sur les bons blocs
- la priorite choisie donne bien le comportement attendu
- les particules affichent bien l'ambiance voulue

---

## Resume court type CurseForge

`Rustling Spots` supporte maintenant des custom spots via datapack.
Vous pouvez creer vos propres spots de biome, definir leurs biomes, blocs, dimensions, priorites, poids, familles Pokemon, familles de loot et particules, sans toucher au code du mod.

Le datapack d'exemple `custom_swamp_spot_pack` est deja fonctionnel et sert de base officielle pour creer vos propres packs.
Il inclut un exemple complet avec :

- un custom swamp spot
- une famille Pokemon custom
- une famille de loot custom
- un nom lisible
- des particules mixees
- une installation simple via `world/datapacks/`

En bref : si vous voulez creer vos propres spots pour un serveur, un modpack ou une map, l'exemple actuel est pret a l'emploi et ce guide couvre l'ensemble du format supporte aujourd'hui.
