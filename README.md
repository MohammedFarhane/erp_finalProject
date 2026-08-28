# ERP — API REST

API de gestion commerciale : catalogue produits, clients, fournisseurs, devis, factures, paiements, commandes fournisseur, suivi de stock et génération de documents PDF. Projet de fin de formation développeur Java (Technifutur, Belgique).

L'accent a été mis sur la cohérence des données métier — stock et paiements calculés depuis un journal plutôt que stockés, montants en `BigDecimal`, transitions d'état contrôlées côté service.

---

## Stack

| | |
|---|---|
| Java | 25 |
| Spring Boot | 4.1.0 |
| Spring Security | 7.1 (JWT via jjwt 0.13) |
| Hibernate | 7.4 |
| PostgreSQL | 18 (conteneur Docker) |
| Migrations | Flyway |
| PDF | Thymeleaf + Open HTML to PDF 1.1.83 |
| Build | Maven multi-modules |

---

## Architecture

Cinq modules Maven, dépendances descendantes :

```
api  →  bll  →  dal  →  dl
 ↓       ↓
        il
```

| Module | Contenu |
|---|---|
| **`dl`** | Entités JPA et enums — le domaine |
| **`dal`** | Repositories Spring Data |
| **`bll`** | Services métier, formulaires, exceptions |
| **`api`** | Contrôleurs REST, DTO, gestion centralisée des erreurs |
| **`il`** | Technique transverse — configuration Spring Security, JWT, horloge |

Quelques partis pris :

- **Aucune entité JPA n'est exposée en REST.** Les contrôleurs échangent des DTO (`XxxRequest` / `XxxResponse`), les services des formulaires (`XxxForm`). Les conversions vivent dans les records eux-mêmes.
- **`open-in-view: false`**, avec `@EntityGraph` sur les requêtes qui en ont besoin — pas de chargement paresseux hors transaction, pas de N+1 masqué.
- **Erreurs centralisées** dans un `@RestControllerAdvice`, réponses au format `ProblemDetail` (RFC 9457).
- **Validation déclarative** via `jakarta.validation` sur les DTO d'entrée.
- **Injection par constructeur** partout.

---

## Modèle métier

### Le flux commercial

```
Devis                          Facture                    Paiements
BROUILLON → ENVOYE → ACCEPTE   BROUILLON → VALIDEE → PAYEE
                  ↘ REFUSE                ↘ ANNULEE
```

L'acceptation d'un devis crée la facture correspondante, **aux montants figés du devis** — aucun recalcul, un prix négocié reste le prix négocié même si le tarif catalogue change entre-temps.

La validation d'une facture décrémente le stock ; son annulation après validation génère des mouvements de retour.

### Le stock n'est pas stocké

Il n'existe aucun champ `quantité` sur `Product`. Le stock est la somme d'un journal de `StockMovement` en ajout seul (`ENTREE`, `SORTIE`, `AJUSTEMENT_POSITIF`, `AJUSTEMENT_NEGATIF`, `RETOUR_CLIENT`), agrégée à la lecture par une requête groupée. Une correction se fait par un mouvement inverse, jamais par une modification.

Le même raisonnement s'applique aux paiements : une facture n'a pas de champ `montant payé`, mais une collection de `Payment` agrégée à la demande. Le paiement partiel en découle naturellement.

### Références

Générées par un compteur annuel par préfixe, avec remise à zéro chaque année :

```
PRD-2026-00001   produit
DEV-2026-00001   devis
FAC-2026-00001   facture
CMD-2026-00001   commande fournisseur
```

L'incrément passe par un `INSERT ... ON CONFLICT DO UPDATE ... RETURNING` — atomique, pas de collision en concurrence.

---

## Documents PDF

Devis, factures et bons de commande sont téléchargeables en PDF :

```
GET /quote/{id}/pdf
GET /billing/{id}/pdf
GET /purchase-order/{id}/pdf
```

Le document est **régénéré à chaque appel** plutôt que stocké : les montants étant figés en base, le rendu est toujours identique. Chaque fichier porte la référence de la pièce — `FAC-2026-00001.pdf`.

Le rendu passe par un gabarit **Thymeleaf** converti en PDF par **Open HTML to PDF** : la mise en page est écrite en HTML/CSS, donc modifiable sans toucher au code Java. Une couleur par type de document — bleu pour la facture, vert pour le devis, orange pour la commande fournisseur — afin d'identifier la pièce d'un coup d'œil. Sur une commande fournisseur, les rôles s'inversent : l'entreprise est l'acheteuse et le fournisseur le destinataire.

Deux contraintes du moteur de rendu, à connaître avant de modifier un gabarit : le HTML doit être **strictement bien formé** (toutes les balises fermées, `&#160;` et non `&nbsp;`), et seul **CSS 2.1** est supporté — ni Flexbox, ni Grid.

### Les coordonnées de l'entreprise

Elles figurent sur chaque document et proviennent d'une entité `Company` — raison sociale, adresse, numéro de TVA, IBAN, contacts. Il n'en existe qu'un seul enregistrement, d'où des routes sans identifiant :

```
GET /company     tout utilisateur authentifié
PUT /company     ADMIN
```

Un déménagement ou un changement d'IBAN se fait donc par l'API, sans redéploiement.

---

## Sécurité

Authentification par jeton JWT, API sans état (`SessionCreationPolicy.STATELESS`).

`POST /auth/login` échange un couple email / mot de passe contre un jeton, à présenter ensuite dans l'en-tête `Authorization: Bearer <jeton>`. Les mots de passe sont hachés en BCrypt.

Deux rôles, `ADMIN` et `EMPLOYEE` :

| | Lecture | Écriture |
|---|---|---|
| Produits, catégories, fournisseurs | tous | `ADMIN` |
| Clients, devis, factures, commandes, stock | tous | tous |
| Coordonnées de l'entreprise | tous | `ADMIN` |
| Utilisateurs | `ADMIN` | `ADMIN` |

Un utilisateur change son propre mot de passe via `PUT /user/change-password` : aucun identifiant n'est accepté dans la requête, il provient du jeton.

Deux garde-fous : impossible d'archiver ou de rétrograder le dernier administrateur actif ; un compte désactivé et un mot de passe erroné renvoient le même message, pour ne pas révéler quels comptes existent.

---

## Prérequis

- Docker — suffit à lui seul pour lancer le projet
- JDK 25 et Maven (wrapper `mvnw` fourni) — uniquement pour développer

**Aucune installation de PostgreSQL n'est nécessaire.** La base tourne dans un conteneur décrit par `docker-compose.yaml`, que Spring démarre lui-même au lancement de l'application — puis attend qu'il soit prêt avant de continuer. Le port publié, la base, l'utilisateur et le mot de passe sont découverts depuis ce fichier : il n'y a aucune URL de connexion à configurer.

Le conteneur publie sur le port **5433** pour ne pas entrer en conflit avec un PostgreSQL déjà installé sur la machine.

---

## Configuration

**Une seule variable d'environnement est requise.** Copiez `.env.example` en `.env` et renseignez-la :

| Variable | Exemple |
|---|---|
| `JWT_SECRET` | `remplacez-moi-par-32-octets-aleatoires-minimum` |

Docker Compose lit ce fichier automatiquement. Pour un lancement depuis l'IDE, définissez plutôt la variable dans la configuration d'exécution.

Elle **doit faire au moins 32 octets** (contrainte de HmacSHA256) et ne jamais être versionnée. Pour en générer une :

```powershell
$rng = New-Object System.Security.Cryptography.RNGCryptoServiceProvider
$bytes = New-Object byte[] 48
$rng.GetBytes($bytes)
[Convert]::ToBase64String($bytes)
```

```bash
openssl rand -base64 48
```

Sous IntelliJ : `Run` → `Edit Configurations…` → `Environment variables`.

---

## Lancement

Deux modes, au choix.

### Tout en conteneurs

```bash
docker compose --profile full up --build
```

Base **et** API démarrent ensemble : ni Java ni Maven ne sont nécessaires, seulement Docker. C'est le mode le plus simple pour découvrir le projet.

### En local, base en conteneur

```bash
./mvnw clean install
./mvnw -pl api spring-boot:run
```

L'application tourne sur la machine, Spring démarre le conteneur PostgreSQL tout seul et attend qu'il soit prêt. C'est le mode de développement : le redémarrage prend quelques secondes au lieu de reconstruire une image.

Le service `api` porte un profil Docker Compose, ce qui l'empêche de démarrer dans ce second mode — sans quoi deux instances se disputeraient le port 8080. Pour la même raison, les deux modes ne se lancent pas simultanément.

Dans les deux cas, l'API écoute sur `http://localhost:8080`.

Le schéma est géré par **Flyway** : au premier démarrage sur une base vide, la migration `V1__init_schema.sql` crée les tables, puis Hibernate — en `ddl-auto: validate` — vérifie qu'elles correspondent aux entités et refuse de démarrer sinon. Les migrations suivantes se déposent dans `api/src/main/resources/db/migration/`.

Toujours au premier démarrage, un jeu de données est inséré : cinq catégories, neuf produits, cinq fournisseurs, deux clients, deux utilisateurs et les coordonnées de l'entreprise. Les lancements suivants n'y touchent plus — **les données sont conservées** dans un volume Docker nommé, qui survit à l'arrêt des conteneurs.

Pour repartir d'une base entièrement vierge :

```bash
docker compose down -v
```

Le `-v` supprime le volume ; Flyway et le jeu de données seront rejoués au démarrage suivant.

### Comptes de démonstration

| Email | Mot de passe | Rôle |
|---|---|---|
| `admin@admin.be` | `test123` | `ADMIN` |
| `employee@employee.be` | `test123` | `EMPLOYEE` |

Comptes de développement, insérés au premier démarrage.

---

## Tests

Trois collections Postman dans `postman/`, 52 requêtes avec assertions :

| Collection | Couvre |
|---|---|
| `quote-flow` | Le cycle de vie d'un devis jusqu'à la facture générée, plus les cas d'erreur |
| `security` | Authentification JWT et matrice d'autorisation |
| `user` | Gestion des comptes, changement de mot de passe, règle du dernier administrateur |

Importer dans Postman, puis `Run collection`. Les collections partent du principe que les identifiants sont ceux du jeu de données initial — pour les rejouer à l'identique, repartir d'une base vierge avec `docker compose down -v`, Flyway et l'`Initializer` la reconstruiront au démarrage suivant.

---

## Endpoints

| Ressource | Base |
|---|---|
| Authentification | `POST /auth/login` |
| Utilisateurs | `/user` |
| Entreprise | `/company` |
| Catégories | `/category` |
| Produits | `/product` |
| Clients | `/client` |
| Fournisseurs | `/supplier` |
| Devis | `/quote` |
| Factures | `/billing` |
| Commandes fournisseur | `/purchase-order` |
| Mouvements de stock | `/stock-movement` |

Les listes sont paginées (`?page=0&size=10`) et filtrables ; les réponses suivent le format `PagedModel`.

Les devis, factures et commandes fournisseur exposent en plus un `GET /{id}/pdf` qui renvoie le document en téléchargement.
