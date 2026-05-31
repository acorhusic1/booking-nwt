// One-shot skript: prebacuje sve *.test.* fajlove iz src/<putanja>/ u src/test/<putanja>/
// i automatski rewrite-uje relativne import putanje da rade iz nove lokacije.
import { readFileSync, writeFileSync, mkdirSync, readdirSync, statSync, rmSync } from 'fs'
import { join, dirname, relative, resolve, posix } from 'path'

const SRC = resolve('src')
const TEST_ROOT = resolve('src/test')

function walk(dir, acc = []) {
  for (const entry of readdirSync(dir)) {
    const full = join(dir, entry)
    if (full === TEST_ROOT) continue
    const st = statSync(full)
    if (st.isDirectory()) walk(full, acc)
    else if (/\.test\.(jsx?|tsx?)$/.test(entry)) acc.push(full)
  }
  return acc
}

const tests = walk(SRC)
console.log(`Pronadjeno ${tests.length} test fajlova.`)

for (const oldPath of tests) {
  const relFromSrc = relative(SRC, oldPath) // npr "components/common/Modal.test.jsx"
  const newPath = join(TEST_ROOT, relFromSrc)
  const newDir = dirname(newPath)
  mkdirSync(newDir, { recursive: true })

  let content = readFileSync(oldPath, 'utf8')
  const oldDir = dirname(oldPath)

  // Rewrite svih relativnih import-a/specifiera (./ ili ../)
  content = content.replace(
    /(from\s+['"]|import\s*\(\s*['"])(\.{1,2}\/[^'"]*)(['"])/g,
    (match, prefix, spec, suffix) => {
      const absTarget = resolve(oldDir, spec) // gdje import pokazuje sa STARE lokacije
      let newSpec = posix.normalize(relative(newDir, absTarget).split('\\').join('/'))
      if (!newSpec.startsWith('.')) newSpec = './' + newSpec
      return `${prefix}${newSpec}${suffix}`
    }
  )

  writeFileSync(newPath, content)
  rmSync(oldPath)
  console.log(`${relFromSrc}  ->  test/${relFromSrc}`)
}

console.log('Gotovo.')
