# Wiki Rustling Spots

> **Documentation ciblée :** Rustling Spots **v4.3** pour Minecraft **1.21.1**.
>
> English: [rustling-spots-wiki.md](./rustling-spots-wiki.md)

---

## Présentation

**Rustling Spots** est un mod d'exploration pour Cobblemon inspiré du système d'herbes frémissantes de **Pokémon Noir & Blanc**.

Il ajoute des spots temporaires dynamiques autour des joueurs pendant l'exploration. Les spots sont représentés directement dans le monde avec des effets visuels et peuvent déclencher une **rencontre Cobblemon**, du **loot thématique**, une **récompense shiny**, ou un **résultat vide** optionnel selon la configuration.

Rustling Spots ne modifie **pas** la génération du monde, ce qui permet de l'ajouter sans régénérer une sauvegarde existante.

---

## Fonctionnalités principales

### Événements dynamiques d'exploration

- Apparition temporaire de spots autour des joueurs pendant l'exploration
- Particules visuelles pour signaler leur présence
- Rencontres Pokémon via Cobblemon
- Loot thématique selon la famille du spot
- Spots temporaires supprimés après interaction ou nettoyage de cycle de vie
- Aucun besoin de régénérer les mondes existants

### Familles de spots intégrées

Rustling Spots comprend **9 familles intégrées**. Chacune possède sa propre identité visuelle, ses règles de surface, ses particules, ses pools Pokémon et ses pools de loot.

| Famille | Surface typique par défaut | Thématique |
| --- | --- | --- |
| **Grass** | `grass_block`, `dirt_path`, petites fleurs | Rencontres classiques d'extérieur |
| **Sand** | `sand`, `red_sand` | Espèces désertiques, fossiles et loot sec |
| **Water** | Eau source avec espace libre au-dessus | Pokémon aquatiques et récompenses liées à l'eau |
| **Snow** | `snow`, `snow_block`, `powder_snow` | Rencontres et récompenses glaciales |
| **Leaves** | Blocs taggés `#leaves` | Rencontres forestières et spots légers |
| **Cave** | Surfaces de pierre ou de deepslate en grotte | Exploration souterraine et loot minier |
| **Flying** | Air libre sous le ciel | Rencontres aériennes et zones en hauteur |
| **NetherFlamme** | `netherrack` ou lave source | Feu et récompenses thématiques du Nether |
| **SoulFlame** | `soul_sand`, `soul_soil` | Ambiance sombre, spectrale et liée aux âmes |

Chaque famille peut être activée, désactivée ou rééquilibrée via configuration.

---

## Fonctionnement en jeu

Une interaction typique fonctionne comme ceci :

1. Un Rustling Spot apparaît à proximité d'un joueur pendant l'exploration.
2. Des particules indiquent sa position.
3. Le joueur s'approche et interagit avec le spot.
4. Le spot se résout en rencontre Cobblemon, loot thématique, résultat vide, ou autre résultat configuré.
5. Le spot est consommé puis supprimé.

Le cycle de vie comprend aussi des limites et du nettoyage côté serveur :

- durée de vie limitée
- suppression après interaction
- suppression possible lorsque les joueurs ne sont plus à proximité
- limites par joueur et globales pour éviter l'accumulation
- depuis la v4.3, les spots actifs sont resynchronisés après connexion, respawn et changement de dimension

### Valeurs par défaut importantes

| Réglage | Valeur par défaut |
| --- | --- |
| Rayon des spots autour du joueur | `200` blocs |
| Distance minimale entre deux spots | `16` blocs |
| Maximum de spots par joueur | `8` |
| Maximum de spots sur le serveur | `64` |
| Durée de vie d'un spot | `6000` ticks, environ 5 minutes |
| Rayon d'interaction | `2` blocs |
| Tolérance verticale d'interaction | `3` blocs |

---

## Récompenses et variantes

Un spot ne se limite pas à un simple spawn Pokémon.

### Résultats possibles

- **Rencontre Cobblemon**
- **Loot thématique**
- **Spot vide** si activé
- **Spot shiny** avec annonce globale optionnelle

### Réglages importants par défaut

| Option | Valeur par défaut |
| --- | --- |
| Chance d'un spot shiny | `0.0025`, soit 0,25 % |
| Annonce globale des découvertes shiny | `true` |
| Spots vides activés | `true` |
| Chance d'un spot vide | `0.02`, soit 2 % |
| Chance de rencontre Pokémon | `0.35`, soit 35 % |
| Niveau Pokémon minimum | `5` |
| Niveau Pokémon maximum | `75` |
| Chance shiny Pokémon par défaut | `0.05`, soit 5 % |
| Récompenses multiples | désactivées par défaut |

Un spot shiny force un résultat shiny lorsque le type de récompense le permet et peut annoncer la découverte au serveur si les annonces globales sont activées.

---

## Configuration

Rustling Spots stocke ses principaux fichiers JSON dans :

```text
config/rustlingspots/
```

| Fichier | Rôle |
| --- | --- |
| `rustlingspots-server.json` | Activation globale, rayon, durée de vie, limites, shiny, spots vides et comportement des récompenses |
| `rustlingspots-pokemon.json` | Chance de rencontre, niveaux et règles de spawn Pokémon |
| `rustlingspots-client.json` | Options d'affichage côté client et préférences de messages |
| `rustlingspots-sound.json` | Volume des sons de récompense |
| `rustlingspots-families.json` | Multiplicateurs d'apparition des familles intégrées |

### Récupération plus sûre des configs en v4.3

Depuis la v4.3, un fichier JSON invalide est sauvegardé avec le suffixe `.invalid.bak` avant la régénération des valeurs par défaut.

Cela évite d'écraser silencieusement un fichier mal formé sans en conserver une copie pour diagnostic.

### Multiplicateurs de spawn par famille

- `grass`: `1.0`
- `sand`: `1.0`
- `water`: `1.0`
- `snow`: `1.0`
- `leaves`: `0.7`
- `cave`: `1.0`
- `flying`: `0.25`
- `netherflamme`: `0.6`
- `soulflame`: `1.0`

### Préférences de messages joueur

Chaque joueur peut contrôler les messages Rustling Spots qu'il reçoit, notamment :

- messages de rencontre Pokémon
- messages de loot
- messages de spot vide

---

## Commandes

### Commandes joueur

```mcfunction
/rustlingspots messages
/rustlingspots messages on
/rustlingspots messages off
/rustlingspots messages pokemon on
/rustlingspots messages loot off
/rustlingspots messages empty on
```

### Commandes admin et debug

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

- `spawn` accepte les familles intégrées comme `grass` et `water`, ainsi que les IDs complets de spots custom
- `spawnshiny` force un spot shiny pour les tests
- `reload` recharge les configs, règles de familles, pools de loot, pools Pokémon et définitions de spots custom
- `stats` expose les statistiques Rustling Spots
- `scan` aide les admins à inspecter les spots actifs dans un rayon proche
- la v4.3 corrige **Total Spots** afin que plusieurs récompenses issues d'un seul spot ne soient plus comptées comme plusieurs spots

---

## Spots custom via datapacks

Rustling Spots comprend un système de spots custom piloté par les données. Les serveurs et modpacks peuvent créer de nouveaux spots sans écrire d'addon Java et sans modifier le worldgen.

Un datapack custom peut définir :

- IDs de spots custom
- dimensions
- biomes
- blocs et règles de surface
- priorité de sélection
- poids de sélection
- noms affichés
- familles Pokémon
- familles de loot
- particules pondérées
- options shiny
- famille visuelle

### `visual_family` en v4.3

Depuis la v4.3, les spots custom peuvent utiliser le champ optionnel `visual_family` afin de sélectionner la base visuelle intégrée la plus adaptée au spot.

Valeurs supportées :

```text
grass
sand
water
snow
leaves
cave
netherflamme
soulflame
flying
```

Si `visual_family` est absent ou invalide, le spot custom conserve le fallback historique `grass`.

Les particules custom restent utilisables pour donner une identité visuelle plus forte à un spot datapack.

### Structure d'un datapack

Le système custom repose sur trois dossiers principaux :

```text
<your_datapack>/
|- pack.mcmeta
`- data/
   `- <namespace>/
      `- rustling_spots/
         |- spot_definitions/
         |- pokemon_families/
         `- loot_families/
```

### Guides complets

- [Custom Spot Definitions Guide, English](./custom-spot-definitions-guide-en.md)
- [Custom Spot Definitions Guide, Français](./custom-spot-definitions-guide.md)

Un datapack d'exemple prêt à l'emploi est fourni dans :

```text
example_datapacks/custom_swamp_spot_pack
```

Cet exemple comprend un spot custom, une famille Pokémon custom, une famille de loot custom et un exemple de particules mixtes.

---

## Compatibilité et sécurité des mondes

- Aucun changement de génération du monde
- Compatible avec les mondes existants
- Builds Fabric et NeoForge maintenus depuis la même base multi-loader
- Les spots peuvent fonctionner dans plusieurs dimensions lorsque les règles de famille ou les définitions custom correspondent
- La dimension de Cobblemon Raid Dens peut être autorisée ou désactivée via configuration
- Les spots custom via datapack sont pilotés par les données et ne nécessitent pas de code Java supplémentaire

---

## Notes de documentation v4.3

Cette documentation v4.3 reflète le code source et les changelogs actuels, notamment :

- support de `visual_family` pour les spots custom
- fallback historique `grass` si `visual_family` est absent ou invalide
- meilleure synchronisation des spots après connexion, respawn et changement de dimension
- sauvegarde `.invalid.bak` des fichiers JSON invalides
- correction du compteur Total Spots avec plusieurs récompenses
- correction de la documentation custom spot concernant le champ de chance shiny supporté

Changelogs spécifiques aux loaders :

- [Fabric changelog](../CHANGELOG-FABRIC.md)
- [NeoForge changelog](../CHANGELOG-NEOFORGE.md)

---

## Résumé

**Rustling Spots** apporte à Cobblemon des événements temporaires d'exploration inspirés de la Génération 5.

Les joueurs découvrent des spots visibles dans le monde et interagissent avec eux pour déclencher des rencontres Pokémon ou des récompenses thématiques. Avec **9 familles intégrées**, une configuration JSON, des outils admin, des statistiques joueur, des spots shiny et vides, ainsi qu'un système datapack complet pour les définitions custom, le mod ajoute une vraie boucle d'exploration configurable sans modifier la génération du monde.
