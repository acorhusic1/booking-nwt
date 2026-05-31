// Druga skripta: rewrite vi.mock('relativna-putanja', ...) u svim test fajlovima
// iz src/test/<...> . Putanju resolvuje iz STARE lokacije (src/<...>) i racuna
// novi relativni path iz nove lokacije.
import { readFileSync, writeFileSync, readdirSync, statSync } from 'fs'
import { join, dirname, relative, resolve, posix } from 'path'

const SRC = resolve('src')
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

const tests = walk(TEST_ROOT)
console.log(`Procesiram ${tests.length} fajlova.`)

let totalFixes = 0

for (const newPath of tests) {
  const relFromTestRoot = relative(TEST_ROOT, newPath) // npr "components/reservations/ReviewModal.test.jsx"
  const oldPath = join(SRC, relFromTestRoot)
  const oldDir = dirname(oldPath)
  const newDir = dirname(newPath)

  let content = readFileSync(newPath, 'utf8')
  let fixCount = 0

  // vi.mock('relativna', factory) — prvi argument je putanja
  content = content.replace(
    /(vi\.mock\s*\(\s*['"])(\.{1,2}\/[^'"]*)(['"])/g,
    (m, prefix, spec, suffix) => {
      const absTarget = resolve(oldDir, spec)
      let newSpec = posix.normalize(relative(newDir, absTarget).split('\\').join('/'))
      if (!newSpec.startsWith('.')) newSpec = './' + newSpec
      fixCount++
      return `${prefix}${newSpec}${suffix}`
    }
  )

  // await import('relativna') — koristen za dohvat mocked modula u testu
  content = content.replace(
    /(await\s+import\s*\(\s*['"])(\.{1,2}\/[^'"]*)(['"])/g,
    (m, prefix, spec, suffix) => {
      const absTarget = resolve(oldDir, spec)
      let newSpec = posix.normalize(relative(newDir, absTarget).split('\\').join('/'))
      if (!newSpec.startsWith('.')) newSpec = './' + newSpec
      fixCount++
      return `${prefix}${newSpec}${suffix}`
    }
  )

  if (fixCount > 0) {
    writeFileSync(newPath, content)
    totalFixes += fixCount
    console.log(`${relFromTestRoot} — ${fixCount} popravki`)
  }
}

console.log(`Ukupno popravki: ${totalFixes}`)
