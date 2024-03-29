/*
 * =====================================================================================
 *
 *       Filename:  Skeleton.cpp
 *
 *    Description:
 *
 *        Version:  1.0
 *        Created:  31/12/17 19:27:38
 *       Revision:  none
 *       Compiler:  gcc
 *
 *         Author:  YOUR NAME (),
 *   Organization:
 *
 * =====================================================================================
 */
#define DEBUG_TYPE "SimpleDCE"
#include "llvm/Pass.h"
#include "llvm/IR/Function.h"
#include "llvm/Support/raw_ostream.h"
#include "llvm/IR/LegacyPassManager.h"
#include "llvm/Transforms/IPO/PassManagerBuilder.h"
#include "llvm/IR/Instructions.h"
#include "llvm/Transforms/Utils/Local.h"
#include "llvm/ADT/SmallVector.h"
#include "llvm/IR/InstIterator.h"

#include "llvm/ADT/Statistic.h"
#include <vector>
#include <iostream>
#include <map>
#include <set>
using namespace llvm;
//using namespace std;
STATISTIC(NumIE, "no. of insts removed");

namespace {
struct SimpleDCE : public FunctionPass {
   std::map<std::string, int> opCounter;
    static char ID;
    int NumIE = 0;
    //int NumIE2 = 0;
    SimpleDCE() : FunctionPass(ID) {}
    virtual bool runOnFunction(Function &F) {
        SmallVector<Instruction*, 64> WL;
/*          SmallVector<Instruction*, 64> ALV;
        for (inst_iterator i = inst_begin(F), e = inst_end(F); i != e; ++i){
          ALV.push_back(&*i);
        } */
    //    std::set<Instruction*> dc;
		  errs() << "I saw a function called " << F.getName() << "!\n";
        errs() << "Function " << F.getName() << '\n';
        for (Function::iterator bb = F.begin(), e = F.end(); bb != e; ++bb) {
//            int numDead = WL.size();
//            int newNum = 0;
            for (BasicBlock::iterator i = bb->begin(), e = bb->end(); i != e; ++i) {
//                  if(!(isa<TerminatorInst>(*i)) && !i->mayHaveSideEffects()){
                  if(isInstructionTriviallyDead(&*i)){
         //           ALV.insert(&*i);
                    WL.push_back(&*i);
                  }
            }
            while (!WL.empty()) {
               Instruction* i = WL.pop_back_val();
               i->eraseFromParent();
               ++NumIE;
           //    for(Use &O : i->operands()){
            //      if(Instruction *i = dyn_cast<Instruction>(O)){
          //           if(ALV.insert(i).second){
             //           WL.push_back(i);
              //       }
               //   }
               //}
            }
        }
        std::map <std::string, int>::iterator i = opCounter.begin();
        std::map <std::string, int>::iterator e = opCounter.end();
        while (i != e) {
            errs() << i->first << ": " << i->second << "\n";
            i++;
        }
        errs() << "\n";
        opCounter.clear();
        //return !WL.empty();
        return false;
    }
};
}
char SimpleDCE::ID = 0;
__attribute__((unused)) static RegisterPass<SimpleDCE>
X("skeletonpass", "Simple dead code elimination"); // NOLINT
static void registerSimpleDCEPass(const PassManagerBuilder &,
                         legacy::PassManagerBase &PM) {
  PM.add(new SimpleDCE());
}
static RegisterStandardPasses
  RegisterMyPass(PassManagerBuilder::EP_EarlyAsPossible,
                 registerSimpleDCEPass);
