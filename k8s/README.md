# Deploying to GKE

Kubernetes manifests (Kustomize) for the banking system. Service discovery is
handled by Kubernetes DNS — no Eureka/Consul. gRPC clients address servers as
`dns:///<service>:<port>`; Kafka and Postgres are reached by their Service DNS
names. All environment-specific values are injected via env vars, so the same
jars run locally (defaults in each `application.yaml`) and on the cluster.

## Layout

```
k8s/
  base/            namespace, postgres, kafka, one Deployment+Service per service, ingress
  overlays/dev/    inherits base; put per-env patches here
```

## Prerequisites

```bash
gcloud config set project YOUR_PROJECT
gcloud services enable container.googleapis.com artifactregistry.googleapis.com cloudbuild.googleapis.com
```

## 1. Container images → Artifact Registry

```bash
LOCATION=us-central1
gcloud artifacts repositories create apps --repository-format=docker --location=$LOCATION
gcloud auth configure-docker $LOCATION-docker.pkg.dev

# 1) Install the whole reactor so sibling jars (common-proto/security/events)
#    are in ~/.m2 for the per-service image builds below. Build under JDK 17;
#    if common-proto errors, see the build note in the repo memory / root README.
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
mvn -DskipTests clean install

# 2) Build each service image with Cloud Native Buildpacks (no Dockerfile).
#    No -am here: deps resolve from ~/.m2, so build-image runs only for the
#    service module (not the library modules).
for s in auth user wallet payment ledger fraud; do
  mvn -q -pl $s-service -DskipTests spring-boot:build-image \
    -Dspring-boot.build-image.imageName=$LOCATION-docker.pkg.dev/YOUR_PROJECT/apps/$s:1.0
  docker push $LOCATION-docker.pkg.dev/YOUR_PROJECT/apps/$s:1.0
done
```

### Or: one command via Cloud Build

[`cloudbuild.yaml`](../cloudbuild.yaml) (repo root) builds the reactor, pushes an
image per service to Artifact Registry with **Jib** (no local Docker), and
optionally deploys:

```bash
gcloud builds submit --config cloudbuild.yaml \
  --substitutions=_LOCATION=$LOCATION,_REPO=apps,_CLUSTER=banking,_TAG=$(git rev-parse --short HEAD)
```

The Cloud Build service account needs **Artifact Registry Writer** and, for the
deploy step, **Kubernetes Engine Developer**. Drop the `deploy` step for a
build-only pipeline; it rewrites the placeholder image refs to the pushed tag
automatically, so you don't need to pre-edit `kustomization.yaml` when deploying
through the pipeline.

## 2. Cluster

```bash
gcloud container clusters create-auto banking --region $LOCATION
gcloud container clusters get-credentials banking --region $LOCATION
```

## 3. Point the manifests at your registry

Edit `k8s/base/kustomization.yaml` → `images:` block, replacing `LOCATION` and
`YOUR_PROJECT`. (Optionally set a real `JWT_SECRET` in the `secretGenerator`.)

## 4. Deploy

```bash
kubectl apply -k k8s/overlays/dev
kubectl -n banking get pods -w
```

Startup order sorts itself out via readiness probes and gRPC/Kafka reconnects
(e.g. ledger retries Kafka, payment retries the gRPC servers).

## 5. Reach it

```bash
kubectl -n banking get ingress banking-ingress      # external IP for /api/auth, /api/payment, /api/ledger
# internal-only services:
kubectl -n banking port-forward svc/user-service 8081:8081
```

Flow to test: `POST /api/auth/register` → login → `POST /api/payment/transfer`
with the bearer token → `GET /api/ledger/accounts/{acct}/transactions`.

## What to harden for production

- **Postgres → Cloud SQL for PostgreSQL.** Drop `base/postgres.yaml`; create a
  Cloud SQL instance with the six databases; add the Cloud SQL Auth Proxy as a
  sidecar to each Deployment and bind a GCP service account via **Workload
  Identity**. Only `SPRING_DATASOURCE_URL` (→ `127.0.0.1:5432`) changes.
- **Kafka → Strimzi or managed** (GCP Managed Service for Apache Kafka /
  Confluent Cloud). Repoint `KAFKA_BOOTSTRAP_SERVERS`.
- **Secrets → Secret Manager** via the External Secrets Operator; remove the
  literal `secretGenerator`.
- **TLS on the ingress** (managed certificate) and switch gRPC off plaintext.
- **Autoscaling** (HorizontalPodAutoscaler) and per-service replica counts in
  the overlay. With >1 replica, gRPC client-side load balancing relies on the
  headless Services already defined here.
