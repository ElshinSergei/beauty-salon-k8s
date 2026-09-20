# setup_cluster.ps1
# Script to quickly restore infrastructure in Minikube

$ErrorActionPreference = "Stop"

Write-Host "--- Starting infrastructure deployment ---" -ForegroundColor Cyan

# 1. Install Istio
Write-Host ">>> Installing Istio..." -ForegroundColor Yellow
if (Test-Path ".\istio-1.23.0\bin\istioctl.exe") {
    .\istio-1.23.0\bin\istioctl install --set profile=demo -y
    kubectl label namespace default istio-injection=enabled --overwrite
} else {
    Write-Error "istioctl.exe not found in .\istio-1.23.0\bin\"
    exit 1
}

# 2. Install ArgoCD
Write-Host ">>> Installing ArgoCD..." -ForegroundColor Yellow
kubectl create namespace argocd --dry-run=client -o yaml | kubectl apply -f -
helm repo add argo https://argoproj.github.io/argo-helm --force-update
helm upgrade --install argocd argo/argo-cd -n argocd --create-namespace

# 3. Create Database Secrets
Write-Host ">>> Creating Database Secrets..." -ForegroundColor Yellow
kubectl create secret generic db-passwords `
    --from-literal=user-db-password=secret_password `
    --from-literal=booking-db-password=booking_password --dry-run=client -o yaml | kubectl apply -f -

# 4. Information
Write-Host "--- Infrastructure ready! ---" -ForegroundColor Green
Write-Host "You can deploy your application with:" -ForegroundColor Cyan
Write-Host "helm install salon-app ./salon-chart" -ForegroundColor White
Write-Host "To get the ArgoCD password, run:" -ForegroundColor Cyan
Write-Host 'kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath=''{.data.password}'' | base64 --decode' -ForegroundColor White
