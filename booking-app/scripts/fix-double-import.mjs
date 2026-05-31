// Popravlja double-applied await-import putanje koje su zavrsile sa 4x ../
// umjesto 3x ../. Heuristika: ako await import("X") ne moze da se resolvuje,
// pomakni jedan level gore.
import { readFileSync, writeFileSync, readdirSync, statSync, existsSync } from 'fs'
import { join, dirname, resolve } from 'path'

const TEST_ROOT = resolve('src/test')

function walk(dir, acc = []) {
  for (const entry of readdirSync(dir)) {
    const full = join(dir, entry)
    const st = statSync(full)
    if (st.isDirectory()) walk(full, acc)
    else if (/\.test\.(jsx?|tsx?)$/.test(entry)) acc.push(full)
  }
  return acc
}

let fixed = 0
for (const path of walk(TEST_ROOT)) {
  let content = readFileSync(path, 'utf8')
  const dir = dirname(path)
  let changed = false

  content = content.replace(
    /(await\s+import\s*\(\s*['"])(\.{1,2}\/[^'"]*)(['"])/g,
    (m, prefix, spec, suffix) => {
      // Pokusaj resolvovati postojeci file (sa raznim ekstenzijama)
      const tryResolve = (s) => {
        const abs = resolve(dir, s)
        return ['', '.js', '.jsx', '.ts', '.tsx', '/index.js', '/index.jsx']
          .some(ext => existsSync(abs + ext))
      }
      if (tryResolve(spec)) return m
      // Pokusaj sa jednim ../ manje
      const shortened = spec.replace(/^\.\.\//, '')
      if (tryResolve(shortened)) {
        changed = true
        return `${prefix}${shortened}${suffix}`
      }
      return m
    }
  )

  if (changed) {
    writeFileSync(path, content)
    fixed++
    console.log(`fix: ${path.replace(TEST_ROOT, 'src/test')}`)
  }
}
console.log(`Popravljeno ${fixed} fajlova.`)
