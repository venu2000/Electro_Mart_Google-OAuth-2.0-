export function formatPrice(value) {
  const amount = Number(value);
  if (Number.isNaN(amount)) return '-';
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(amount);
}

export function stockLabel(stock) {
  if (stock === 0) return 'Out of stock';
  if (stock <= 5) return `Only ${stock} left`;
  return 'In stock';
}
