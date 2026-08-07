---
## Presentation

**Rustling Spots** est un mod pour *Cobblemon* inspire du systeme d'herbes fremissantes de **Pokemon Noir & Blanc**.
Il ajoute des evenements temporaires dynamiques apparaissant autour des joueurs, accompagnes de particules.

Interagir avec un spot declenche une rencontre Cobblemon ou l'obtention d'un loot thematique selon sa famille.
Le mod ne modifie pas la generation du monde, ce qui le rend sur pour les sauvegardes existantes.

---
## Fonctionnalites principales

### Evenements dynamiques

- Apparition temporaire de spots autour des joueurs pendant l'exploration
- Indices visuels pour signaler leur presence
- Rencontres exclusivement Cobblemon, sans mobs vanilla lies au systeme
- Recompenses thematiques selon la famille du spot
- Fonctionnement compatible avec les mondes deja existants

### Familles de spots

Le mod propose **neuf familles** distinctes, chacune avec son identite visuelle, ses particules, ses regles de surface et ses pools de Pokemon / loot configurables.

| Famille | Surface par defaut | Thematique |
| --- | --- | --- |
| **Grass** | `grass_block`, `dirt_path`, petites fleurs | Rencontres classiques d'exterieur |
| **Sand** | `sand`, `red_sand` | Especes desertiques, fossiles, loot sec |
| **Water** | Eau source avec air au-dessus | Pokemon aquatiques et objets lies a l'eau |
| **Snow** | `snow`, `snow_block`, `powder_snow` | Especes glaciales et recompenses hivernales |
| **Leaves** | Blocs tagges `#leaves` | Rencontres forestieres et spots legers |
| **Cave** | Pierre / deepslate avec air en grotte | Exploration souterraine, loot minier |
| **Flying** | Zone aerienne ouverte sous le ciel | Rencontres aeriennes et ambiance de hauteur |
| **NetherFlamme** | `netherrack` ou lave source | Chaleur, feu, ambiance du Nether |
| **SoulFlame** | `soul_sand`, `soul_soil` | Ambiance sombre, maudite, spirituelle |

> Chaque famille peut etre activee, desactivee ou reequilibree via configuration.

---
## Objectif du mod

- Introduire une boucle d'exploration inspiree de la Generation 5 (*Pokemon Noir & Blanc*)
- Ajouter des evenements interactifs visibles directement dans le monde
- Encourager le deplacement et l'observation active plutot qu'un simple spawn passif
- Donner plus de personnalite aux environnements via des rencontres et recompenses contextualisees

---
## Fonctionnement en jeu

Lorsqu'un joueur explore le monde :

1. Un Rustling Spot peut apparaitre a proximite quand le joueur progresse dans de nouveaux chunks.
2. Des particules signalent sa presence.
3. Le joueur s'approche et interagit avec le spot.
4. Le spot declenche une rencontre Cobblemon, un loot, ou parfois aucun resultat selon la configuration.
5. Le spot disparait ensuite automatiquement.

Le systeme respecte aussi un cycle de vie simple :

- les spots ont une duree de vie limitee
- ils disparaissent apres interaction
- ils disparaissent aussi s'il n'y a plus de joueur a proximite
- ils respectent des limites par joueur et globales pour eviter l'encombrement du serveur
- depuis la v4.3, les spots actifs sont resynchronises apres connexion, respawn ou changement de dimension

### Valeurs par defaut importantes

| Parametre | Valeur par defaut |
| --- | --- |
| Rayon de presence autour du joueur | `200` blocs |
| Distance minimale entre deux spots | `16` blocs |
| Nombre maximum de spots par joueur | `8` |
| Nombre maximum de spots sur le serveur | `64` |
| Duree de vie d'un spot | `6000` ticks (~5 minutes) |
| Rayon d'interaction | `2` blocs |
| Tolerance verticale d'interaction | `3` blocs |

---
## Variantes et recompenses

Le systeme ne se limite pas a un simple spawn Pokemon.

### Types de resultats possibles

- **Rencontre Cobblemon**
- **Loot thematique**
- **Spot vide** si cette option est activee
- **Spot shiny** avec annonce globale possible

### Reglages par defaut

| Option | Valeur par defaut |
| --- | --- |
| Chance qu'un spot soit shiny | `0.0025` (0.25%) |
| Annonce globale des trouvailles shiny | `true` |
| Spots vides actives | `true` |
| Chance d'un spot vide | `0.02` (2%) |
| Chance de rencontre Pokemon | `0.35` (35%) |
| Niveau Pokemon min / max | `5` a `75` |
| Chance shiny Pokemon par defaut | `0.05` (5%) |
| Recompenses multiples | desactivees par defaut |

Un spot shiny garantit un resultat shiny cote recompense, et peut en plus annoncer la decouverte a tout le serveur si l'option est laissee active.

---
## Configuration

Le mod expose plusieurs fichiers JSON pour ajuster precisement son comportement :

| Fichier | Role |
| --- | --- |
| `config/rustlingspots/rustlingspots-server.json` | Activation globale, rayon, duree de vie, limites, shiny, spots vides, recompenses multiples |
| `config/rustlingspots/rustlingspots-pokemon.json` | Chances de rencontre, niveaux, regles de spawn Pokemon |
| `config/rustlingspots/rustlingspots-client.json` | Ombres, preferences d'affichage des messages cote client |
| `config/rustlingspots/rustlingspots-sound.json` | Volumes des sons de recompense |
| `config/rustlingspots/rustlingspots-families.json` | Taux d'apparition par famille |

Depuis la v4.3, un fichier JSON invalide est sauvegarde avec un suffixe `.invalid.bak` avant que les valeurs par defaut soient regenerees.

### Multiplicateurs de spawn par famille

Les multiplicateurs par defaut sont actuellement :

- `grass`: `1.0`
- `sand`: `1.0`
- `water`: `1.0`
- `snow`: `1.0`
- `leaves`: `0.7`
- `cave`: `1.0`
- `flying`: `0.25`
- `netherflamme`: `0.6`
- `soulflame`: `1.0`

### Preferences de messages joueur

Chaque joueur peut aussi choisir quels messages il souhaite voir :

- messages de rencontre Pokemon
- messages de loot
- messages de spot vide

---
## Commandes utiles

### Commandes joueur

```mcfunction
/rustlingspots messages
/rustlingspots messages on
/rustlingspots messages off
/rustlingspots messages pokemon on
/rustlingspots messages loot off
/rustlingspots messages empty on
```

### Commandes admin / debug

```mcfunction
/rustlingspots spawn grass
/rustlingspots spawn rustlingspots:grass
/rustlingspots spawn <namespace:spot_id>
/rustlingspots spawnshiny rustlingspots:grass
/rustlingspots reload
/rustlingspots stats
/rustlingspots stats <player>
/rustlingspots scan 64
/rustlingspots scan 64 grass
```

Notes utiles :

- `spawn` accepte les familles integrees (`grass`, `water`, etc.) mais aussi les IDs complets de spots custom
- `spawnshiny` force une version shiny pour les tests
- `reload` recharge les configs, regles de familles, pools de loot, pools Pokemon et definitions de spots custom
- `scan` sert au debug pour lister les spots actifs proches

---
## Personnalisation avancee

Le mod supporte aussi des **custom spots via datapack** :

- spots personnalises par biome / bloc / dimension
- familles Pokemon custom
- familles de loot custom
- particules ponderees personnalisees
- priorite et poids de selection

Un datapack peut donc ajouter ses propres spots sans addon Java ni modification du worldgen.

Guide detaille :

- [Custom Spot Definitions Guide](./custom-spot-definitions-guide.md)

Un datapack d'exemple est deja fourni dans :

- `example_datapacks/custom_swamp_spot_pack`

---
## Compatibilite et securite

- Aucun changement de worldgen
- Compatible avec les sauvegardes existantes
- Fonctionne dans plusieurs dimensions tant qu'une famille ou un spot custom peut correspondre
- Compatibilite prevue avec la dimension de **Cobblemon Raid Dens**, activable ou desactivable via config
- Les spots custom restent des ajouts de donnees, sans besoin de code additionnel

---
## Resume court

**Rustling Spots** apporte a *Cobblemon* un systeme de spots temporaires inspire de **Pokemon Noir & Blanc**.
Le joueur repere un evenement visuel et sonore, s'en approche, puis declenche une rencontre ou une recompense thematique.

Avec ses **9 familles de spots**, ses **configurations JSON**, ses **commandes de debug**, ses **stats joueur**, ses **spots shiny**, ses **spots vides** et son **support datapack**, le mod ajoute une vraie boucle d'exploration dynamique sans toucher a la generation du monde.
